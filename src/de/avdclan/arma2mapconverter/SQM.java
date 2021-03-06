package de.avdclan.arma2mapconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import de.avdclan.arma2mapconverter.Synchronizable.SubTypes;

import org.apache.log4j.Logger;

public class SQM {
	//TODO: Verify that player groups are not spawned by the headless.
	private TypeClass rootType = new TypeClass("units", null);
	private TypeClass markers = new TypeClass("markers", null);
	private TypeClass triggers = new TypeClass("triggers", null);
	//private TypeClass vehicles = new TypeClass("vehicles", null);
	private ArrayList<Item> modules = new ArrayList<Item>();
	private static Logger logger = Logger.getLogger(SQM.class);
	private BufferedReader reader;
	private int groupCountWest = 0;
	private int groupCountEast = 0;
	private int groupCountGuer = 0;
	private int groupCountCiv = 0;
	private File source;
	private MissionTrimmer missionTrimmer;
	private ArrayList<String> publicNames = new ArrayList<String>();
	
	public void load(File mission) throws FileNotFoundException {
		//TODO: Integrate with SQMParser
		logger.debug("Loading SQM Mission: " + mission.getAbsolutePath());
		missionTrimmer = new MissionTrimmer(mission.getAbsolutePath());
		this.source = mission;
		reader = new BufferedReader(new FileReader(mission));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				String input = line.replaceAll("^\\s+", "");
				String type = null;
				if (input.startsWith("class")) {

					String[] spl = line.split(" ", 2);
					type = spl[1];
				}
				if (type != null) {
					if (type.equals("Groups")) {
						logger.debug("Processing groups... ");
						parse(line, rootType);
						logger.debug("Groups processed. "
								+ rootType.getFullCount()
								+ " Groups processed.");
					}
					if (type.equals("Markers")) {
						logger.debug("Processing markers... ");
						parse(line, markers);
						logger.debug("Markers processed. "
								+ markers.getFullCount()
								+ " Markers processed.");
					}
					if (type.equals("Sensors")) {
						logger.debug("Processing triggers... ");
						parse(line, triggers);
						logger.debug("triggers processed. "
								+ triggers.getFullCount()
								+ " triggers processed.");
					}
					/*if (type.equals("Vehicles")) {
						logger.debug("Processing empty vehicles... ");
						parse(line, vehicles);
						logger.debug("vehicles processed. "
								+ vehicles.getFullCount()
								+ " vehicles processed.");
					}*/
				}

			}
		} catch (IOException e) {
			logger.error(e);
		}

		logger.debug("Loaded.");
	}

	public MissionTrimmer getMissionTrimmer() {
		return missionTrimmer;
	}
	
	/**
	 * This is the parsing algorithm. If you know a better way, feel free to
	 * change it.
	 * 
	 * Please also send your changes to the author.
	 * 
	 * 
	 * @param input
	 * @throws IOException
	 */
	private void parse(String input, TypeClass parent) throws IOException {
		String line = input.replaceAll("^\\s+", "");
		if (line.startsWith("class")) {

			String[] spl = line.split(" ", 2);
			TypeClass typeClass = new TypeClass(spl[1], parent);
			parent.getChilds().add(typeClass);

			while (!(line = reader.readLine().replaceAll("^\\s+", ""))
					.startsWith("}")) {
				parse(line, typeClass);
			}

		}
		if (parent.getType().equals("Groups")) {

		}
		if (parent.toString().startsWith("Vehicles")) {
			if (parent.getObject() == null) {
				parent.setObject(new Vehicle());
			}
			TypeClass p = parent.getParent();
			if (p.toString().startsWith("Item")) {

				((Vehicle) parent.getObject()).setSide(((Item) p.getObject())
						.getSide());

			}

		} else if (parent.toString().startsWith("Waypoints")) {
			Waypoints waypoints = new Waypoints();
			if (parent.getObject() == null) {
				parent.setObject(waypoints);
			}
			TypeClass p = parent.getParent();
			if (p.toString().startsWith("Item")) {

				((Waypoints) parent.getObject()).setSide(((Item) p.getObject())
						.getSide());

			}

		} else if (parent.toString().startsWith("Markers")) {
			if (parent.getObject() == null) {
				parent.setObject(new Markers());
			}
			TypeClass p = parent.getParent();
			if (p.toString().startsWith("Item")) {

				((Markers) parent.getObject()).setSide(((Item) p.getObject())
						.getSide());

			}

		} else if (parent.toString().startsWith("Sensors")) {
			if (parent.getObject() == null) {
				parent.setObject(new Triggers());
			}
			TypeClass p = parent.getParent();
			if (p.toString().startsWith("Item")) {

				((Triggers) parent.getObject()).setSide(((Item) p.getObject())
						.getSide());

			}

		} else if (parent.toString().startsWith("Item")) {
			if (parent.getObject() == null) {
				parent.setObject(new Item(parent.toString()));
			}
			if (line.startsWith("position[]=")) {
				String[] tmp = line.split("=", 2);
				tmp = tmp[1].split(",", 3);
				String x = tmp[0].replaceAll("\\{", "");
				String z = tmp[1];
				String y = tmp[2].replaceAll("\\}\\;", "");
				((Item) parent.getObject())
						.setPosition(new Position(x, y, z));
			}
			if (line.startsWith("id=")) {
				String[] tmp = line.split("=", 2);
				String id = tmp[1].replaceAll("\\;", "");
				((Item) parent.getObject()).setId(id);
			} else if (line.startsWith("side=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setSide(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("vehicle=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setVehicle(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("skill=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setSkill(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("leader=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setLeader(tmp[1].replaceAll("\\;",
						""));
			}  else if (line.startsWith("player=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setPlayer(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("init=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setInit(tmp[1]);
			} else if (line.startsWith("name=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setName(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("markerType=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setMarkerType(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("type=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setType(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("rank=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setRank(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("presenceCondition=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setPresenceCondition(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("azimut=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setAzimut(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("colorName=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setColorName(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("fillName=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setFillName(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("a=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setA(tmp[1].replaceAll("\\;", ""));
			} else if (line.startsWith("b=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setB(tmp[1].replaceAll("\\;", ""));
			} else if (line.startsWith("angle=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setAngle(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("text=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setText(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("rectangular=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setRectangular(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("age=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject())
						.setAge(tmp[1].replaceAll("\\;", ""));
			} else if (line.startsWith("activationBy=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setActivationBy(tmp[1].replaceAll(
						"\\;$", ""));
			} else if (line.startsWith("expCond=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setExpCond(tmp[1].replaceAll(
						"\\;$", ""));
			} else if (line.startsWith("expActiv=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setExpActiv(tmp[1].replaceAll(
						"\\;$", ""));
			} else if (line.startsWith("expDesactiv=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setExpDesactiv(tmp[1].replaceAll(
						"\\;$", ""));
			} else if (line.startsWith("interruptable=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setInterruptable(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("activationType=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setActivationType(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("timeoutMin=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setTimeoutMin(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("timeoutMid=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setTimeoutMid(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("timeoutMax=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setTimeoutMax(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("placement=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setPlacement(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("completionRadius=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setCompletionRadius(tmp[1]
						.replaceAll("\\;", ""));
			} else if (line.startsWith("combatMode=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setCombatMode(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("formation=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setFormation(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("speed=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setSpeed(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("combat=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setCombat(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("description=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setDescription(tmp[1].replaceAll(
						"\\;", ""));
			} else if (line.startsWith("showWP=")) {
				String[] tmp = line.split("=", 2);
				((Item) parent.getObject()).setShowWP(tmp[1].replaceAll("\\;",
						""));
			} else if (line.startsWith("synchronizations[]=")) {
				((Item) parent.getObject()).setSyncArray(line);
			}
		} else {
			// unsupported class
		}

	}

	public SQF toSQF() {
		SQF sqf = new SQF();
		String code = ""
				+ "/**\n"
				+ " * Converted with Arma2MapConverter v"
				+ Arma2MapConverter.VERSION
				+ "\n"
				+ " *\n"
				+ " * Source: "
				+ source.getAbsolutePath()
				+ "\n"
				+ " * Date: "
				+ DateFormat.getInstance().format(
						Calendar.getInstance().getTime()) + "\n" + " */\n\n";
		code += "private[\"_westHQ\",\"_eastHQ\",\"_guerHQ\",\"_civHQ\",\"_createdUnits\",\"_wp\"];\n\n";
		code += "_westHQ = createCenter west;\n"
				+ "_eastHQ = createCenter east;\n"
				+ "_guerHQ = createCenter resistance;\n"
				+ "_civHQ  = createCenter civilian;\n";

		code += "\n_createdUnits = [];\n";

		//code += "\n/*******************\n" + " * MARKER CREATION *\n"
		//		+ " *******************/\n";
		//code += generateSQF(markers);
		//code += "\n/*****************\n" + " * EMPTY VEHICLE CREATION *\n"
		//		+ " *****************/\n";
		//code += generateSQF(vehicles); 
		code += "\n/*****************\n" + " * UNIT CREATION *\n"
				+ " *****************/\n";
		code += generateSQF(rootType);
		code += "\n/********************\n" + " * TRIGGER SYNCHRONIZATION *\n"
				+ " ********************/\n";
		code += generateSQF(triggers);
		code += "\n/********************\n" + " * MODULE SYNCHRONIZATION *\n"
				+ " ********************/\n";
		code += generateModuleSyncSQF();
		code += "\n/**************************\n"
				+  "* BROADCAST PUBLIC NAMES *\n"
				+  "**************************/\n";
		code += generateBroadcastSQF();
		code += "\n// return all created units in an array\n"
				+ "[_createdUnits]\n";
		//TODO: broadcast unit names
		sqf.setCode(code);
		return sqf;
	}

	private String generateSQF(TypeClass typeClass) {
		String code = "";

		for (TypeClass tc : typeClass.getChilds()) {

			if (tc.equals("Markers")) {
				for (TypeClass items : tc.getChilds()) {
					Item item = (Item) items.getObject();
					if (item.getName() == null) {
						// generate unique unit name
						item.setName("marker_"
								+ UUID.randomUUID().toString()
										.replaceAll("-", ""));
					}
					code += "_marker = createMarker[" + item.getName() + ", ["
							+ item.getPosition().getX() + ", "
							+ item.getPosition().getY() + "]];\n"
							+ "_marker setMarkerShape " + item.getType()
							+ ";\n" + "_marker setMarkerType "
							+ item.getMarkerType() + ";\n"
							+ "_marker setMarkerSize [" + item.getA() + ", "
							+ item.getB() + "];\n";

					if (item.getText() != null) {
						code += "_marker setMarkerText " + item.getText()
								+ ";\n";
					}
					if (item.getColorName() != null) {
						code += "_marker setMarkerColor " + item.getColorName()
								+ ";\n";
					}
					if (item.getFillName() != null) {
						code += "_marker setMarkerBrush " + item.getFillName()
								+ ";\n";
					}
					code += "_createdMarkers = _createdMarkers + [_marker];\n";
					code += "\n";

				}
			}
			if (tc.equals("Sensors")) {
				for (TypeClass items : tc.getChilds()) {

					Item item = (Item) items.getObject();
					item.generatePublicName();
					item.setSubtype(SubTypes.TRIGGER);
					missionTrimmer.updateTrigger(item);
					code += item.getSyncSQF() + "\n";
					/*
					if (item.getName() == null) {
						item.setName("_trg");
					}
					item.setName(item.getName().replaceAll("\"", ""));
					String cond;
					if(item.getExpCond().equals("true")) {
						cond = "this";
					} else {
						cond = item.getExpCond();
					}
						
					code += item.getName()
							+ " = createTrigger[\"EmptyDetector\", "
							+ item.getPosition() + "];\n" + item.getName()
							+ " setTriggerArea[" + item.getA() + ", "
							+ item.getB() + ", " + item.getAngle() + ", "
							+ item.getRectangular() + "];\n" + item.getName()
							+ " setTriggerActivation[" + item.getActivationBy()
							+ ", " + item.getActivationType() + ", "
							+ item.getRepeating() + "];\n" + item.getName()
							+ " setTriggerStatements[\"" + cond
							+ "\", " + item.getExpActiv() + ", "
							+ item.getExpDesactiv() + "];\n" + item.getName()
							+ " setTriggerTimeout[" + item.getTimeoutMin()
							+ ", " + item.getTimeoutMid() + ", "
							+ item.getTimeoutMax() + ", "
							+ item.getInterruptable() + "];\n";
					if (item.getText() != null) {
						code += item.getName() + " setTriggerText "
								+ item.getText() + ";\n";
					}
					code += "_createdTriggers = _createdTriggers + ["
							+ item.getName() + "];\n";
					*/

				}
			}
			if (tc.equals("Waypoints")) {
				int index = 0;
				String groupName = null;
				for (TypeClass tClass : tc.getParent().getChilds()) {
					if (tClass.getType().equals("Vehicles")) {
						groupName = ((Vehicle) tClass.getObject())
								.getGroupName();
					}
				}
				logger.debug("Adding waypoints for group " + groupName);
				code += "\n/**\n" + " * Waypoints for group " + groupName
						+ "\n" + " */\n";
				for (TypeClass items : tc.getChilds()) {
					++index;
					Item item = (Item) items.getObject();
					item.setSubtype(SubTypes.WAYPOINT);
					code += "// waypoint #" + index + "\n";
					code += "_wp = " + groupName + " addWaypoint[["
							+ item.getPosition().getX() + ", "
							+ item.getPosition().getY() + ", 0], "
							+ item.getPlacement() + ", " + index + "];\n";
					String wp = "[" + groupName + ", " + index + "]";
					item.setName(wp);
					if (item.getCombat() != null) {
						code += wp + " setWaypointBehaviour "
								+ item.getCombat() + ";\n";
					}
					if (item.getCombatMode() != null) {
						code += wp + " setWaypointCombatMode "
								+ item.getCombatMode() + ";\n";
					}
					if (item.getCompletionRadius() != null) {
						code += wp + " setWaypointCompletionRadius "
								+ item.getCompletionRadius() + ";\n";
					}
					if (item.getFormation() != null) {
						code += wp + " setWaypointFormation "
								+ item.getFormation() + ";\n";
					}
					if (item.getSpeed() != null) {
						code += wp + " setWaypointSpeed " + item.getSpeed()
								+ ";\n";
					}
					if (item.getExpCond() != null) {
						code += wp + " setWaypointStatements[\""
								+ item.getExpCond() + "\", " + item.getExpActiv()
								+ "];\n";
					}	
					if (item.getType() != null) {
						code += wp + " setWaypointType " + item.getType()
								+ ";\n";
					}
					code +=  item.getSyncSQF()  + "\n";
				}

			}
			if (tc.equals("Vehicles")) {

				String side = ((Vehicle) tc.getObject()).getSide()
						.toLowerCase();
				String group = "_group_" + side + "_" + getGroupCound(side);
				((Vehicle) tc.getObject()).setGroupName(group);
				code += "// group " + group + "\n";
				code += "private[\""+group+"\"];\n";
				code += group + " = createGroup _" + side + "HQ;\n";

				for (TypeClass items : tc.getChilds()) {

					Item item = (Item) items.getObject();
					//Do not include player slots or modules
					if ( item.getPlayer() != null ) {
						continue;
					}
					if (item.getSide().equals("LOGIC")) {
						//Registers unit as a logic module for syncing
						//Assures logic module has a name so it can be
						//referenced in the SQF-script
						modules.add(item);
						missionTrimmer.updateModule(item);
						continue;
					}
					item.setSubtype(SubTypes.UNIT);
					
					//Delete unit from the mission.sqm
					missionTrimmer.deleteUnit(item.getId());

					code += "// begin " + item.getName() + ", part of group "
							+ group + "\n";
					if (item.nameIsPrivate()) {
						//Auto-generated units are referenced through private vars
						code += "private[\"" + item.getName() + "\"];\n";
					} else {
						//Public name
						publicNames.add(item.getName());
					}
					code += "if (" + item.getPresenceCondition() + ") then\n{\n";
					if (item.getSide().equals("EMPTY")) {
						code += "\t" + item.getName()
								+ " = createVehicle [" + item.getVehicle()
								+ ", " + item.getPosition()
								+ ", [], 0, " + item.getSpecial() + "];\n";
					} else {
						code += "\n" + "\t"
								+ item.getName()
								+ " = "
								+ group
								+ " createUnit ["
								+ item.getVehicle()
								+ ", "
								+ item.getPosition()
								+ ", [], 0, \"CAN_COLLIDE\"];\n"
								// this is VERY dirty and only used because I don't want to create\n"
								// arrays for vehicles, units and stuff to check if the classname\n"
								// is a vehicle, an unit, and so on. this just works.\n"
								// what it does is if the unit is not alive after creation (because it should be a manned vehicle)\n"
								// it will be created with createVehicle and manned with the BIS_fnc_spawnCrew function.\n"
								+ "\t// Did not spawn as a unit try as a vehicle\n"
								+ "\tif(!alive " + item.getName()
								+ ") then {\n" + "\t\t" + item.getName()
								+ " = createVehicle [" + item.getVehicle()
								+ ", "
								+ item.getPosition()
								+ ", [], 0, \"CAN_COLLIDE\"];\n"
								// + "\t\t_group = createGroup _"
								// + item.getSide().toLowerCase() + "HQ;\n"
								+ "\t\t[" + item.getName()
								// + ", _group] call BIS_fnc_spawnCrew;\n"
								+ ", " + group + "] call BIS_fnc_spawnCrew;\n"
								+ "\t};\n\n";

					}
					// Fix: setVehicleInit is not supported any more.
//					if (item.getInit() != null) {
//						code += "\t" + item.getName() + " setVehicleInit "
//								+ item.getInit() + ";\n";
//					}
					if (item.getAzimut() != null) {
						code += "\t" + item.getName() + " setDir "
								+ item.getAzimut() + ";\n";
					}
					if (item.getSkill() != null
							&& !item.getSide().equals("EMPTY")) {
						code += "\t" + item.getName() + " setUnitAbility "
								+ item.getSkill() + ";\n";
					}
					if (item.getRank() != null
							&& !item.getSide().equals("EMPTY")) {
						code += "\t" + item.getName() + " setRank "
								+ item.getRank() + ";\n";
					}
					if (item.getLeader() != null
							&& !item.getSide().equals("EMPTY")) {
						code += "\t" + group + " selectLeader " + item.getName() + ";\n";
					}
					if (item.getInit() != null) {
						code += "\t" + fixInitCode(item.getInit()).replace("this",item.getName()) + "\n";
					}
					code +=  "\t" + item.getSyncSQF() + "\n";
//					if (item.getText() != null
//							&& !item.getSide().equals("EMPTY")) {
//						code += "\tsetVehicleVarName \"" + item.getText().replace("\"", "") + "\";\n";
//					}
					
					code += "\t_createdUnits = _createdUnits + ["
							+ item.getName() + "];\n";
					code += "};\n// end of " + item.getName() + "\n";

				}

			} else {
				code += generateSQF(tc);
			}
		}

		return code;
	}

	private String generateModuleSyncSQF() {
		String code = "";
		for (Item module : modules) {
			module.generatePublicName();
			module.setSubtype(SubTypes.UNIT);
			code += module.getSyncSQF();
		}
		return code;
	}
	
	private String getGroupCound(String side) {
		if (side.equals("west")) {
			++groupCountWest;
			return String.valueOf(groupCountWest);
		}
		if (side.equals("east")) {
			++groupCountEast;
			return String.valueOf(groupCountEast);
		}
		if (side.equals("guer")) {
			++groupCountGuer;
			return String.valueOf(groupCountGuer);
		}

		++groupCountCiv;
		return String.valueOf(groupCountCiv);

	}

	private String generateBroadcastSQF() {
		String rVal = "";
		for (String name : publicNames) {
			rVal += "publicVariable \"" + name + "\";" + "\n";
		}
		return rVal;
	}
	
	private String fixInitCode(String text){
		String result = new String(text);
		
		if (result.startsWith("\""))
			result = result.substring(1);
		
		if (result.endsWith("\";"))
			result = result.substring(0, result.length()-2);

		//Init does not need to end with ";" but the code needs to.
		if (!result.endsWith(";"))
			result = result+";";
		
		//Replace Double quotes with single quotes
		result = result.replaceAll("\"\"","\"");
		
		return result;
	}
}
