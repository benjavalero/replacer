#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import unicode_literals

import bz2
import xml.sax
import re

class DumpHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.title = ""
        self.ns = ""
        self.text = ""
        self.section = ""

        # Regex to find a year
        self.regex = re.compile(r'\b(1\d{3}|20[01]\d|202[01])\b')

    # Call when an element starts
    def startElement(self, tag, attributes):
        self.CurrentData = ""

    # Call when a character is read
    def characters(self, content):
        self.CurrentData += content

    # Call when an elements ends
    def endElement(self, tag):
        if tag == "ns":
            self.ns = self.CurrentData.strip()
        elif tag == "title":
            self.title = self.CurrentData.strip()
        elif tag == "text":
            self.text = self.CurrentData.strip()
        elif tag == "page":
            # Restart section
            self.section = ""

            # Only process articles and annexes
            if self.ns in ['0', '104']:
                self.checkListsInText(self.text)

    def checkListsInText(self, text):
        # Read the wikitext line by line looking for lists
        # For each list found we process it to find years and check if they are well ordered

        listLines = []
        for line in text.splitlines():
            line = line.strip()
            if line.startswith('*'):
                listLines.append(line)
            elif line.startswith('=') and line.endswith('='):
                self.section = line.replace('=', '').strip()
            elif line:
                # If empty line we continue
                # If not empty then the list has ended and we process it
                if len(listLines):
                    numUnordered = self.findNumberOfUnorderedItems(listLines)
                    if numUnordered > 0:
                        # DEBUG
                        # print('---------------')
                        # print(self.title, '###', self.section)
                        # print('----')
                        # print("\n".join(listLines))
                        # print('---------------')

                        print('\t'.join([self.title, self.section, str(len(listLines)), str(numUnordered)]))
                        return

                # Continue with next list
                listLines = []

    # Return -1 in case of list to be ignored
    # Return  0 in case of list well ordered
    # Return  n in case of list with n items unordered
    def findNumberOfUnorderedItems(self, listLines):
        years = []
        for line in listLines:
            # Exclude lists containing templates
            if "{{" in line:
                return -1

            lineYear = self.findYearInLine(line)
            if lineYear:
                years.append(lineYear)
            else:
                return -1

        # Only take into account lists with 3 or more items
        if len(years) <= 2:
            return -1

        # Check order in year list
        numUnordered = 0
        maxYear = 0
        for year in years:
            if year < maxYear:
                numUnordered += 1
            else:
                maxYear = year
        return numUnordered

    def findYearInLine(self, line):
        results = re.findall(self.regex, line)
        if len(results) == 1:
            return int(results[0][0])
        else:
            return None


if ( __name__ == "__main__"):
    # create an XMLReader
    parser = xml.sax.make_parser()
    # turn off namepsaces
    parser.setFeature(xml.sax.handler.feature_namespaces, 0)

    # override the default ContextHandler
    Handler = DumpHandler()
    parser.setContentHandler(Handler)

    parser.parse(bz2.BZ2File("/Users/benja/Developer/eswiki/20210601/eswiki-20210601-pages-articles.xml.bz2", "r"))
