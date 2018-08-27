package hu.sze.jkk.auto.track.mapgeneration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class TrajectoryRead {
	final List<ControlVertex> cvs;
	
	private double road_width = 0.0;
	private String road_name = ""; 
	
	private final int rejectionrate;
	// XML generation
	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	
	public TrajectoryRead(final int rejection_rate){
		cvs = new ArrayList<>();
		rejectionrate = rejection_rate;		
	}
	private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private static final XMLEvent endline = eventFactory.createDTD("\n");
	private static final XMLEvent tabline = eventFactory.createDTD("\t");
	
	public void WriteXML(OutputStream os) throws XMLStreamException
	{
		XMLEventWriter eventWriter = outputFactory
                .createXMLEventWriter(os);
        
        
        // start SDF        
        //StartDocument startDocument = eventFactory.createStartDocument();
        //eventWriter.add(startDocument);
        
        StartElement configStartElement = eventFactory.createStartElement("", "", "sdf");
        eventWriter.add(configStartElement);
        eventWriter.add(eventFactory.createAttribute("version", "1.4"));
        eventWriter.add(endline);
        StartElement worldElement = eventFactory.createStartElement("", "", "world");
        eventWriter.add(tabline);
        eventWriter.add(worldElement);
        eventWriter.add(eventFactory.createAttribute("name", road_name));
        eventWriter.add(endline);
        // Nodes
        // Create a control point
        StartElement roadStartElement = eventFactory.createStartElement("","", "road");
        eventWriter.add(tabline);
        eventWriter.add(roadStartElement);
        eventWriter.add(eventFactory.createAttribute("name", road_name));
        eventWriter.add(endline);        
        for (ControlVertex cv: cvs)
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append(cv.getX()).append(' ');
        	sb.append(cv.getY()).append(' ');
        	sb.append(cv.getZ());
        	eventWriter.add(tabline);
        	createNode(eventWriter, "point", sb.toString());
        }
        EndElement worldElement_end = eventFactory.createEndElement("", "", "world");
        eventWriter.add(worldElement_end);
        EndElement roadEndElement = eventFactory.createEndElement("", "", "road");
        eventWriter.add(roadEndElement);
        eventWriter.add(eventFactory.createEndElement("", "", "sdf"));
        eventWriter.add(eventFactory.createEndDocument());
        eventWriter.close();

	}
	
	public void SdfXmlWrite(String path) throws FileNotFoundException, XMLStreamException {
		WriteXML(new FileOutputStream(path));
	}
	
	private void createNode(XMLEventWriter eventWriter, String name,
            String value) throws XMLStreamException {

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        
        // create Start node
        StartElement sElement = eventFactory.createStartElement("", "", name);
        eventWriter.add(tabline);
        eventWriter.add(sElement);
        // create Content
        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);
        // create End node
        EndElement eElement = eventFactory.createEndElement("", "", name);
        eventWriter.add(eElement);
        eventWriter.add(endline);

    }
	
	public final int getRejectionRate(){
		return rejectionrate;
	}
	
	public void constructRoadTrajectory(String name, double width){
		if (cvs.size()!=0){
						
		}
	}
	
	public void readTrajectoryFromCSV(String path) throws IOException {
		try (
			Reader reader = Files.newBufferedReader(Paths.get(path));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
		){
			int i = 0;
			for (CSVRecord record: csvParser) {
				if (i % rejectionrate == 0) {
					cvs.add(new ControlVertex(
							Double.parseDouble(record.get(0)),
							Double.parseDouble(record.get(1)),
							Double.parseDouble(record.get(2))
					));
					i = 0;
				}
				i++;
			}
		}
		
	}
	
	public int cvCount() {
		return cvs.size();
	}
	
	
}
