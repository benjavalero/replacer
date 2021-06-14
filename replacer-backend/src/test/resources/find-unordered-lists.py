#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import unicode_literals

import bz2
import xml.sax
import re

YEAR_REGEX = re.compile(r'\b(1\d{3}|20[01]\d|202[01])\b')
PROCESSABLE_NAMESPACES = ['0', '104']
DEFAULT_SECTION = "Sección inicial"

class DumpHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.title = ""
        self.ns = ""
        self.text = ""

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
            # Only process articles and annexes
            if self.ns in PROCESSABLE_NAMESPACES:
                self.processPage()

    def processPage(self):
        # Find all lists in page as an array of pairs section-listLines
        foundLists = self.findListsInText()

        # Filter unordered lists and find (if existing) the one with more unordered items
        maxUnorderedList = None
        maxNumItems = 0
        for foundList in foundLists:
            numUnordered = self.findNumberOfUnorderedItems(foundList[1])
            if (numUnordered > maxNumItems):
                maxUnorderedList = foundList
                maxNumItems = numUnordered

        # TODO
        # 3. Find all tables in page
        # 4. Filter unordered tables and find (if existing) the one with more unordered rows
        # 5. Find the list/table with more unordered items. If existing, print the result.
        # NOTE: WE NEED TO KEEP THE SECTION DURING ALL THE STEPS

        if maxUnorderedList:
            print('\t'.join([self.title, maxUnorderedList[0], str(len(maxUnorderedList[1])), str(maxNumItems)]))

    def findListsInText(self):
        foundLists = []

        section = DEFAULT_SECTION
        listLines = []
        for line in self.text.splitlines():
            line = line.strip()
            if line.startswith('*'):
                listLines.append(line)
            elif line.startswith('=') and line.endswith('='):
                section = line.replace('=', '').strip()
            elif line:
                # If empty line we continue
                # If not empty then the list has ended and we process it
                if len(listLines):
                    foundLists.append([section, listLines])

                # Continue with next list
                listLines = []
        return foundLists


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
        results = re.findall(YEAR_REGEX, line)
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
