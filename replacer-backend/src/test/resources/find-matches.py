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
        self.regex = re.compile("\\|([^=|]+?)=\\s*\\d{2} de (?:enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre) de \\d{4}")

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
                for res in re.finditer(self.regex, self.text):
                    print(res.group(1).strip())

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