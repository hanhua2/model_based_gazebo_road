package hu.sze.jkk.auto.track.mapgeneration.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import hu.sze.jkk.auto.track.mapgeneration.TrajectoryRead;

class TestBasicMapgeneration {
	private static final int RECORD_CNT = 1942;
	private static final int REJECTION_RATE = 25;

	@Test
	void testBasicRead() {
		
		TrajectoryRead tr = new TrajectoryRead(REJECTION_RATE);
		try
		{
			tr.readTrajectoryFromCSV("./Shell-eco-2018-xy-940m.csv");
			assertEquals((RECORD_CNT/REJECTION_RATE) + 1, tr.cvCount());
		}catch(IOException io) {
			fail("Should not catch IOException");
		}
	}
	
	@Test
	void testBasicSDFSerialization() {
		
		TrajectoryRead tr = new TrajectoryRead(REJECTION_RATE);
		try
		{
			tr.readTrajectoryFromCSV("./Shell-eco-2018-xy-940m.csv");
			assertEquals((RECORD_CNT/REJECTION_RATE) + 1, tr.cvCount());
			tr.WriteXML(System.out);
		}catch(IOException io) {
			fail("Should not catch IOException");
		}catch(XMLStreamException xo) {
			fail("Should not catch XMLStreamException");
		}
		
	}
}
