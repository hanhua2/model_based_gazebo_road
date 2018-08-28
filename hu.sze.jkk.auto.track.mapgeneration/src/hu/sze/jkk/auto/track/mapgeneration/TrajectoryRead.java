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
	
	private String world_name = "default";
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
        StartDocument startDocument = eventFactory.createStartDocument();
        eventWriter.add(startDocument);
        eventWriter.add(endline);
        StartElement configStartElement = eventFactory.createStartElement("", "", "sdf");
        eventWriter.add(configStartElement);
        eventWriter.add(eventFactory.createAttribute("version", "1.4"));
        eventWriter.add(endline);
        // Setup world
        StartElement worldElement = eventFactory.createStartElement("", "", "world");
        eventWriter.add(tabline);
        eventWriter.add(worldElement);
        eventWriter.add(eventFactory.createAttribute("name", world_name));
        eventWriter.add(endline);
        // Include basic SDF stuff
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createStartElement("", "", "include"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        createNode(eventWriter, "uri", "model://sun");
        eventWriter.add(eventFactory.createEndElement("", "", "include"));
        eventWriter.add(tabline);
        eventWriter.add(endline);
        appendGroundPlane(eventWriter);
        // Nodes
        // Create a control point
        StartElement roadStartElement = eventFactory.createStartElement("","", "road");
        eventWriter.add(tabline);
        eventWriter.add(roadStartElement);
        eventWriter.add(eventFactory.createAttribute("name", road_name));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        createNode(eventWriter, "width", Double.toString(road_width));
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
	
	private static void createNode(XMLEventWriter eventWriter, String name,
            String value) throws XMLStreamException {

        //XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        
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
	
	public static void appendGroundPlane(XMLEventWriter eventWriter) throws XMLStreamException {
		eventWriter.add(tabline);
        eventWriter.add(eventFactory.createStartElement("", "", "model"));
        eventWriter.add(eventFactory.createAttribute("name", "ground"));
        eventWriter.add(endline);        
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createStartElement("", "", "link"));
        eventWriter.add(eventFactory.createAttribute("name", "body"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createStartElement("", "", "collision"));        
        eventWriter.add(eventFactory.createAttribute("name", "geom"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createStartElement("", "", "geometry"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createStartElement("", "", "plane"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        createNode(eventWriter, "normal", "0 0 1");
        eventWriter.add(eventFactory.createEndElement("", "", "plane"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createEndElement("", "", "geometry"));
        eventWriter.add(eventFactory.createEndElement("", "", "collision"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        eventWriter.add(eventFactory.createEndElement("", "", "link"));
        eventWriter.add(endline);
        eventWriter.add(tabline);
        createNode(eventWriter, "static", "true");
        eventWriter.add(eventFactory.createEndElement("", "", "model"));
        eventWriter.add(tabline);
        eventWriter.add(endline);
	}
	
	public void constructRoadTrajectory(String name, double width, boolean close){
		if (cvs.size()!=0){
			this.road_name = name;
			this.road_width = width;
			if (close) {
				cvs.add(cvs.get(0));
			}
		}
	}
	
	public void constructRoadTrajectory(String name, double width){
		constructRoadTrajectory(name, width, false);
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
							0.0
							//Double.parseDouble(record.get(2))
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
