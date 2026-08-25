#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Utility module to find and parse Wikipedia dump files.

Supports both the old single-file format and the new chunked format:
  Old: {base}/{YYYYMMDD}/{project}-{YYYYMMDD}-pages-articles.xml.bz2
  New: {base}/{YYYY-MM-DD}/xml/bzip2/{project}-{YYYY-MM-DD}-*.xml.bz2

Usage:
    from dump_utils import parse_dump

    # Parse all chunks of the latest dump sequentially
    parse_dump(handler, base_path="/Users/benja/Developer/eswiki", project="eswiki")
"""

import bz2
import os
import re
import sys
import xml.sax
from pathlib import Path


# Date folder patterns
_NEW_DATE_PATTERN = re.compile(r'\d{4}-\d{2}-\d{2}')  # 2026-07-01
_OLD_DATE_PATTERN = re.compile(r'\d{8}')               # 20260701


def find_dump_files(base_path, project="eswiki"):
    """
    Find the dump files for the latest available date under base_path.
    Tries the new chunked structure first, then falls back to the old single-file format.

    Returns a sorted list of Path objects to .xml.bz2 files.
    """
    base = Path(base_path)
    if not base.exists():
        raise FileNotFoundError(f"Dump base path does not exist: {base_path}")

    # Collect date folders matching new format (YYYY-MM-DD)
    new_folders = sorted(
        [d for d in base.iterdir() if d.is_dir() and _NEW_DATE_PATTERN.fullmatch(d.name)],
        reverse=True
    )

    for folder in new_folders:
        bzip2_dir = folder / "xml" / "bzip2"
        if bzip2_dir.is_dir():
            # Date in new format: 2026-07-01
            date_str = folder.name
            prefix = f"{project}-{date_str}-"
            files = sorted([
                f for f in bzip2_dir.iterdir()
                if f.name.startswith(prefix) and f.name.endswith(".xml.bz2")
            ])
            if files:
                return files

    # Fallback: old format (YYYYMMDD)
    old_folders = sorted(
        [d for d in base.iterdir() if d.is_dir() and _OLD_DATE_PATTERN.fullmatch(d.name)],
        reverse=True
    )

    for folder in old_folders:
        date_str = folder.name
        prefix = f"{project}-{date_str}-pages-articles"
        files = sorted([
            f for f in folder.iterdir()
            if f.name.startswith(prefix) and f.name.endswith(".xml.bz2")
        ])
        if files:
            return files

    raise FileNotFoundError(f"No dump files found under: {base_path}")


def parse_dump(handler, base_path="/Users/benja/Developer/eswiki", project="eswiki"):
    """
    Find the latest dump files and parse them sequentially with the given SAX handler.

    The handler will receive startDocument/endDocument calls for each chunk file.
    """
    dump_files = find_dump_files(base_path, project)
    print(f"Found {len(dump_files)} dump file(s):", file=sys.stderr)
    for f in dump_files:
        print(f"  {f.name}", file=sys.stderr)

    parser = xml.sax.make_parser()
    parser.setFeature(xml.sax.handler.feature_namespaces, 0)
    parser.setContentHandler(handler)

    for dump_file in dump_files:
        print(f"Parsing: {dump_file.name} ...", file=sys.stderr)
        parser.parse(bz2.BZ2File(str(dump_file), "r"))

    print("Done.", file=sys.stderr)
