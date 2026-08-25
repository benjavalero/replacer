#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import unicode_literals

import xml.sax
import re
from dump_utils import parse_dump

class DumpHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = []
        self.ns = ""
        self.text = ""
        self.regex = re.compile(r'\b[Ss]ólo\b')

    # Call when an element starts
    def startElement(self, tag, attributes):
        self.CurrentData.clear()

    # Call when an elements ends
    def endElement(self, tag):
        if tag == "ns":
            self.ns = ''.join(self.CurrentData).strip()
        elif tag == "text":
            self.text = ''.join(self.CurrentData)
        elif tag == "page":
            if self.ns in ('0', '104'):
                for line in self.text.splitlines():
                    if self.regex.search(line):
                        print(line)

    # Call when a character is read
    def characters(self, content):
        self.CurrentData.append(content)

if ( __name__ == "__main__"):
    parse_dump(DumpHandler())
