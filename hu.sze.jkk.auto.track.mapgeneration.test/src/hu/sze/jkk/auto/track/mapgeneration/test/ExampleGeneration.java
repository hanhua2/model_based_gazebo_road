package hu.sze.jkk.auto.track.mapgeneration.test;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import hu.sze.jkk.auto.track.mapgeneration.TrajectoryRead;

public class ExampleGeneration {

	public static void main(String[] args) {		
		TrajectoryRead tr = new TrajectoryRead(5);
		try {
			tr.readTrajectoryFromCSV("./Shell-eco-2018-xy-940m.csv");
			tr.constructRoadTrajectory("road_world", 6);
			tr.WriteXML(new FileOutputStream(new File("/home/szakkor81/jkk_git/szenergy-gazebo/src/szelectricity_worlds/world/road_example.sdf")));
		}catch (IOException io) {
			io.printStackTrace();
		}catch (XMLStreamException xo) {
			xo.printStackTrace();
		}
	}

}
