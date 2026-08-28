"""Copy a slice of the PvZ2 asset dump into assets/pvz/.

Reads a manifest from tools/manifests/<name>.json:

    {
      "pams":     ["768/INITIAL/WORLDMAP/LEVEL_NODE/LEVEL_NODE.PAM"],
      "ids":      ["IMAGE_ENDLEVEL_EGYPT_TROPHY"],
      "prefixes": ["IMAGE_WORLDMAP_EGYPT_ISLAND"],
      "groups":   ["ZOMBIE_TUTORIAL"]
    }

Every image a listed PAM references is pulled in automatically, so "pams" is
usually all you need.  Atlases are merged region by region into a transparent
canvas of the same size, which keeps the bundle small and lets several
manifests share one atlas without overwriting each other.

    python tools/extract_assets.py worldmap-egypt            # dry run
    python tools/extract_assets.py worldmap-egypt --apply
"""

import io
import json
import os
import re
import shutil
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
SRC = os.path.join(os.path.dirname(REPO), "pvz-assets") + os.sep
DST = os.path.join(REPO, "assets", "pvz") + os.sep
RES = "768"


def load(path):
    return json.load(io.open(path, encoding="utf-8"))


def index(source):
    by_id = {}
    group_of = {}
    for group in source["groups"]:
        res = group.get("res")
        if res is not None and str(res) != RES:
            continue
        for resource in group.get("resources", []):
            if resource.get("id"):
                by_id[resource["id"]] = resource
                group_of[resource["id"]] = group
    return by_id, group_of


def longest_known(token, by_id):
    if token in by_id:
        return token
    for cut in range(1, 5):
        if token[:-cut] in by_id:
            return token[:-cut]
    return token


def wanted_ids(manifest, by_id):
    wanted = set(manifest.get("ids", []))
    for prefix in manifest.get("prefixes", []):
        for rid in by_id:
            if rid.startswith(prefix):
                wanted.add(rid)
    for group in manifest.get("groups", []):
        stem = "IMAGE_ZOMBIE_" + group + "_" + group + "_"
        for rid in by_id:
            if rid.startswith(stem):
                wanted.add(rid)
    missing = []
    for rel in manifest.get("pams", []):
        full = SRC + "IMAGES/" + rel
        if not os.path.exists(full):
            missing.append(rel)
            continue
        raw = io.open(full, "rb").read()
        for match in set(re.findall(rb"IMAGE_[A-Z0-9_]{4,}", raw)):
            wanted.add(longest_known(match.decode(), by_id))
    if missing:
        raise SystemExit("PAM files not found:\n  " + "\n  ".join(missing))
    return wanted


def merge_index(bundle, source, by_id, group_of, resolved):
    keep = {}
    for rid in resolved:
        keep.setdefault(str(group_of[rid].get("id")), []).append(rid)

    existing = {str(g.get("id")): g for g in bundle["groups"]}
    regions = {}
    added = 0
    for gid, ids in sorted(keep.items()):
        src_group = next(g for g in source["groups"] if str(g.get("id")) == gid)
        target = existing.get(gid)
        if target is None:
            target = {k: v for k, v in src_group.items() if k != "resources"}
            target["resources"] = []
            existing[gid] = target
            bundle["groups"].append(target)
        have = {r.get("id") for r in target["resources"]}
        for rid in sorted(ids):
            entry = by_id[rid]
            parent = entry.get("parent")
            if parent and parent not in have and by_id.get(parent):
                target["resources"].append(by_id[parent])
                have.add(parent)
                added += 1
            if rid not in have:
                target["resources"].append(entry)
                have.add(rid)
                added += 1
            if parent and entry.get("aw"):
                regions.setdefault(str(parent), []).append(entry)
    return regions, added, len(keep)


def atlas_file(parent):
    name = re.sub(r"^ATLASIMAGE_ATLAS_", "", str(parent))
    for candidate in (name + ".PNG", name + ".png"):
        full = SRC + "ATLASES/" + candidate
        if os.path.exists(full):
            return full, candidate
    return None, name + ".PNG"


def write(manifest, bundle, regions):
    for rel in manifest.get("pams", []):
        out = DST + "IMAGES/" + rel
        if not os.path.exists(out):
            os.makedirs(os.path.dirname(out), exist_ok=True)
            shutil.copy2(SRC + "IMAGES/" + rel, out)

    for parent, entries in sorted(regions.items()):
        src_path, name = atlas_file(parent)
        if not src_path:
            raise SystemExit("no source atlas for " + parent)
        src_img = Image.open(src_path).convert("RGBA")
        dst_path = DST + "ATLASES/" + name
        if os.path.exists(dst_path):
            canvas = Image.open(dst_path).convert("RGBA")
            if canvas.size != src_img.size:
                raise SystemExit("atlas size drift for " + name)
        else:
            canvas = Image.new("RGBA", src_img.size, (0, 0, 0, 0))
        for entry in entries:
            box = (entry["ax"], entry["ay"],
                   entry["ax"] + entry["aw"], entry["ay"] + entry["ah"])
            canvas.paste(src_img.crop(box), box)
        os.makedirs(os.path.dirname(dst_path), exist_ok=True)
        canvas.save(dst_path, optimize=True)

    io.open(DST + "RESOURCES.json", "w", encoding="utf-8", newline="\n").write(
        json.dumps(bundle, indent=1, ensure_ascii=False) + "\n")


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if not args:
        raise SystemExit(__doc__)
    name = args[0]
    apply_changes = "--apply" in sys.argv

    manifest = load(os.path.join(HERE, "manifests", name + ".json"))
    source = load(SRC + "RESOURCES.json")
    bundle = load(DST + "RESOURCES.json")

    by_id, group_of = index(source)
    wanted = wanted_ids(manifest, by_id)
    resolved = {i for i in wanted if i in by_id}
    unresolved = sorted(wanted - resolved)

    regions, added, groups = merge_index(bundle, source, by_id, group_of, resolved)

    print("manifest           %s" % name)
    print("PAM files          %d" % len(manifest.get("pams", [])))
    print("image ids wanted   %d  (resolved %d, unresolved %d)"
          % (len(wanted), len(resolved), len(unresolved)))
    if unresolved:
        print("  unresolved sample:", unresolved[:6])
    print("groups touched     %d" % groups)
    print("atlases touched    %d" % len(regions))
    print("index entries added %d" % added)

    if not apply_changes:
        print("\nDRY RUN - nothing written. re-run with --apply")
        return

    write(manifest, bundle, regions)
    total = sum(os.path.getsize(os.path.join(d, f))
                for d, _, fs in os.walk(DST) for f in fs)
    print("\nbundle now %.1f MB" % (total / 1e6))


main()
