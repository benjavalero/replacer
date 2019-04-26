#!/usr/bin/python

import bz2
import xml.sax

class DumpHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.title = ""
        self.ns = ""
        self.timestamp = ""
        self.text = ""

    # Call when an element starts
    def startElement(self, tag, attributes):
        self.CurrentData = ""

    # Call when an elements ends
    def endElement(self, tag):
        if tag == "title":
            self.title = self.CurrentData.strip()
        elif tag == "ns":
            self.ns = self.CurrentData.strip()
        elif tag == "timestamp":
            self.timestamp = self.CurrentData.strip()
        elif tag == "text":
            self.text = self.CurrentData.strip()
        elif tag == "page" and self.ns == "0":
            str = '%s\t%s\t%s' % (self.title, len(self.text), self.timestamp)
            print str.encode('utf-8')

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

    print "Title\tLength\tTimestamp"

    parser.parse(bz2.BZ2File('/Users/benja/Developer/pywikibot/20190401/eswiki-20190401-pages-meta-current.xml.bz2'))