package edu.sc.seis.seisFile.stationxml;

import java.util.ArrayList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class Network {

    public Network(final XMLEventReader reader) throws XMLStreamException, StationXMLException {
        StartElement startE = StaxUtil.expectStartElement(StationXMLTagNames.NETWORK, reader);
        netCode = StaxUtil.pullAttribute(startE, StationXMLTagNames.NET_CODE);
        while (reader.hasNext()) {
            XMLEvent e = reader.peek();
            if (e.isStartElement()) {
                String elName = e.asStartElement().getName().getLocalPart();
                if (elName.equals(StationXMLTagNames.STARTDATE)) {
                    startDate = StaxUtil.pullText(reader, StationXMLTagNames.STARTDATE);
                } else if (elName.equals(StationXMLTagNames.ENDDATE)) {
                    endDate = StaxUtil.pullText(reader, StationXMLTagNames.ENDDATE);
                } else if (elName.equals(StationXMLTagNames.DESCRIPTION)) {
                    description = StaxUtil.pullText(reader, StationXMLTagNames.DESCRIPTION);
                } else if (elName.equals(StationXMLTagNames.TOTALNUMSTATIONS)) {
                    totalNumStations = StaxUtil.pullInt(reader, StationXMLTagNames.TOTALNUMSTATIONS);
                } else if (elName.equals(StationXMLTagNames.SELECTEDNUMSTATIONS)) {
                    selectedNumStations = StaxUtil.pullInt(reader, StationXMLTagNames.SELECTEDNUMSTATIONS);
                } else if (elName.equals(StationXMLTagNames.STATION)) {
                    stations = new StationIterator(reader);
                    break;
                } else {
                    StaxUtil.skipToMatchingEnd(reader);
                }
            } else if (e.isEndElement()) {
                reader.nextEvent();
                return;
            } else {
                e = reader.nextEvent();
            }
        }
    }
    
    public String getNetCode() {
        return netCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public StationIterator getStations() {
        return stations;
    }
    
    public int getTotalNumStations() {
        return totalNumStations;
    }

    public int getSelectedNumStations() {
        return selectedNumStations;
    }

    String netCode, startDate, endDate, description;

    int totalNumStations, selectedNumStations;
    
    StationIterator stations = new ListStationIterator(new ArrayList<Station>()); // init to empty
}
