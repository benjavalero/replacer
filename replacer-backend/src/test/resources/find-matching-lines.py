#!/usr/bin/python
# -*- coding: utf-8 -*-

from __future__ import unicode_literals

import bz2
import xml.sax
import re

class DumpHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.ns = ""
        self.text = ""
        self.regex = re.compile(r'\b[Ss]ólo\b')

    # Call when an element starts
    def startElement(self, tag, attributes):
        self.CurrentData = ""

    # Call when an elements ends
    def endElement(self, tag):
        if tag == "ns":
            self.ns = self.CurrentData.strip()
        elif tag == "text":
            self.text = self.CurrentData.strip()
        elif tag == "page":
            if self.ns in ['0', '104']:
                for line in self.text.splitlines():
                    if re.search(self.regex, line):
                        print(line)

    # Call when a character is read
    def characters(self, content):
        self.CurrentData += content

if ( __name__ == "__main__"):
    # create an XMLReader
    parser = xml.sax.make_parser()
    # turn off namepsaces
    parser.setFeature(xml.sax.handler.feature_namespaces, 0)

    # override the default ContextHandler
    Handler = DumpHandler()
    parser.setContentHandler(Handler)

    parser.parse(bz2.BZ2File("/Users/benja/Developer/pywikibot/20190701/eswiki-20190701-pages-articles.xml.bz2", "r"))
