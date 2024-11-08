#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import bz2
import xml.sax

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
    # create an XMLReader
    parser = xml.sax.make_parser()
    # turn off namepsaces
    parser.setFeature(xml.sax.handler.feature_namespaces, 0)

    # override the default ContextHandler
    Handler = DumpHandler()
    parser.setContentHandler(Handler)

    print("ID\tNS\tLength\tTimestamp\tRedirect")

    parser.parse(bz2.BZ2File("/Users/benja/Developer/eswiki/20241020/eswiki-20241020-pages-articles.xml.bz2", "r"))
