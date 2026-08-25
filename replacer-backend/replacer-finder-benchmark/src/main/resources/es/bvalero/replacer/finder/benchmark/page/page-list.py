#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import xml.sax

# Import dump_utils from the shared location
_script_dir = os.path.dirname(os.path.abspath(__file__))
_project_root = _script_dir
while _project_root != '/' and not os.path.exists(os.path.join(_project_root, 'pom.xml')):
    _project_root = os.path.dirname(_project_root)
sys.path.insert(0, os.path.join(_project_root, 'replacer-backend/replacer-finder/src/test/resources'))
from dump_utils import parse_dump

class DumpHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.id = ""
        self.title = ""
        self.ns = ""
        self.redirect = False
        self.timestamp = ""
        self.text = ""

    # Call when an element starts
    def startElement(self, tag, attributes):
        self.CurrentData = ""

    # Call when an elements ends
    def endElement(self, tag):
        if tag == "id":
            # ID appears several times (contributor, revision, etc). We care about the first one.
            if (self.id == ""):
                self.id = self.CurrentData.strip()
        if tag == "title":
            self.title = self.CurrentData.strip()
        elif tag == "ns":
            self.ns = self.CurrentData.strip()
        elif tag == "redirect":
            self.redirect = True
        elif tag == "timestamp":
            self.timestamp = self.CurrentData.strip()
        elif tag == "text":
            self.text = self.CurrentData.strip()
        elif tag == "page":
            str = '%s\t%s\t%s\t%s\t%s' % (self.id, self.ns, len(self.text), self.timestamp[:10], self.redirect)
            print(str)
            self.id = ""
            self.redirect = False

    # Call when a character is read
    def characters(self, content):
        self.CurrentData += content

if ( __name__ == "__main__"):
    print("ID\tNS\tLength\tTimestamp\tRedirect")
    parse_dump(DumpHandler())
