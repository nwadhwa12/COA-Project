package apexsimulatorextended;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;

import common.CircularQueue;

public class ApexSimulatorExtended {

	/**
	 * constants and global variables
	 */
	final static String fetch = "Fetch Stage";
	final static String decode1 = "Decode1/Dispatch Stage";
	final static String decode2 = "Decode2/Rename Stage";
	final static String issueQ = "IssueQueue";
	final static String execute1 = "Int ALU Execute1", execute2 = "Int ALU Execute2";
	final static String multiply = "Multiply ALU", LSFU1 = "LSFU1", LSFU2 = "LSFU2";
	final static String branchALU = "Branch ALU";
	final static String writeALU = "IntALU Writeback", writeMultiply = "MultiplyALU Writeback",
			writeLSFU = "LSFU Writeback";
	public static int ZeroFlag = 0;
	public static boolean HALTFLAG = false;
	public static int programCounter = 4000;
	public static HashMap Instruction;
	public static HashMap<Integer, String> InstructionMap = new HashMap<Integer, String>();
	public static LinkedHashMap<String, Integer> unifiedRegisterFile = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, RenameTable> renameTable = new LinkedHashMap<String, RenameTable>();
	public static LinkedHashMap<String, RenameTable> r_rat = new LinkedHashMap<String, RenameTable>();
	public static int memory[] = new int[4000];
	public static Scanner sc = new Scanner(System.in);
	public static int sizeUrf = 32;
	public static List<IQ> issueQueue = new ArrayList<IQ>(12);
	public static CircularQueue ROB = new CircularQueue(16);
	public static HashMap<String, Instructions> pipeline = new LinkedHashMap<String, Instructions>();
	public static boolean BranchTaken = false;
	public static boolean isStall = false;
	public static int BranchPcValue;
	public static int NextInstructionBAL;
	public static Queue allocationList = new LinkedList();
	public static final int ALU = 1;
	public static final int MUL = 2;
	public static final int LSFU = 3;
	public static final int Branch = 4;
	static int index = 0;
	public static int resultLSFU1;
	public static int literal_zero = 0000;
	public static int source1ALU, source2ALU, source1MUL, source2MUL, source1Branch, source2Branch, source1LSFU,
	source2LSFU;
	public static int counter = 4;
	public static boolean branchStall = false;
	public static IQ putInQ = new IQ();
	public static boolean loadStoreRobHead = false;
	public static String insbranch;
	public static IQ mulIQ;

	public static int cyclenumber;
	public static int numberofInsprocessed;
	public static int stalledDispatch;
	public static int noissue,loadCommited, storeCommited;

	/*
	 * This function take the file path as a argument read the file line by line
	 * and put the instructions in an Hash Map
	 */
	public static void ReadInstructionfile(String path) throws IOException {

		FileReader fr = new FileReader(path);
		BufferedReader reader = new BufferedReader(fr);
		int i = 0;
		int instructionCount = 0;
		int pc = programCounter;
		while (reader.readLine() != null) {
			instructionCount++;
		}
		fr = new FileReader(path);
		reader = new BufferedReader(fr);
		for (i = 0; i < instructionCount; i++) {
			InstructionMap.put(pc, reader.readLine());
			pc = pc + 4;
		}
		reader.close();

	}

	/**
	 * Main method
	 */
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		do {
			System.out.print("Press 1 to Initialize\n");
			System.out.print("Press 2 Simulate(n) Intruction\n");
			System.out.print("Press 3 Set_URF_Size\n");
			System.out.print("Press 4 Print_map_tables\n");
			System.out.print("Press 5 Print_IQ\n");
			System.out.print("Press 6 Print_ROB\n");
			System.out.print("Press 7 Print_URF\n");
			System.out.print("Press 8 Print_Memory <a1> <a2>\n");
			System.out.print("Press 9 Print_Pipeline\n");
			System.out.println("Press 10 Print IPC");
			System.out.print("Press 11 Exit\n");

			System.out.print("Please Enter your choice:\n\n");

			int choice = sc.nextInt();

			switch (choice) {
			case 1:
				// Initialize Register and Memory
				Initialize(args[0]);
				break;

			case 2:
				// Simulate the Instruction
				Simulate();
				break;

			case 3:
				// Sets the custom URF size
				changeUrfSize();
				break;

			case 4:
				printMapTables();
				break;

			case 5:
				printIQ();
				break;

			case 6:
				printROB();
				break;

			case 7:
				printURF();
				break;

			case 8:
				printMemory();
				break;

			case 9:
				printPipeline();
				break;

			case 10:
				printStats();
				break;

			case 11:
				System.exit(1);
				sc.close();
				break;
			}
		} while (true);
	}

	private static void printStats() {
		//		if(pipeline.get(fetch)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(execute1)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(execute2)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(issueQ)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(execute1)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(execute2)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(multiply)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(branchALU)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(LSFU1)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(LSFU2)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(writeALU)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(writeLSFU)!=null)
		//			numberofInsprocessed++;
		//		if(pipeline.get(writeMultiply)!=null)
		//			numberofInsprocessed++;
		Float resultipc = (float) ((float) (numberofInsprocessed)/(float)cyclenumber);
		System.out.println("Number of ins:"+numberofInsprocessed);
		System.out.println("Number of cycles:"+ cyclenumber);
		System.out.println("IPC:" + resultipc);
		resultipc = (float) (numberofInsprocessed/(7+numberofInsprocessed-1));
		System.out.println("Dispatch stalled:"+resultipc);
		System.out.println("No issue:" + noissue);
		System.out.println("LOAD committed:"+ loadCommited);
		System.out.println("STORE committed:"+ storeCommited);

	}

	public static void Initialize(String path) throws IOException {
		ReadInstructionfile(path);
		if (sizeUrf >= 32) {
			for (int i = 0; i < 16; i++) {
				unifiedRegisterFile.put("R" + i, 0);
			}

			for (int j = 0; j < sizeUrf - 17; j++) {
				unifiedRegisterFile.put("P" + j, 0);
				allocationList.add("P" + j);
			}

			unifiedRegisterFile.put("X", 0);

			for (int k = 0; k < 4000; k++) {
				memory[k] = 0;
			}

			initializePipeline();
			System.out.println("All Initialization Complete\n");
		} else
			System.out.println("Initialization cannot be done, URF size should be greater than default size:32");

	}

	public static void Simulate() {

		System.out.println("Please enter the total number of cycles you want to simulate:");
		Scanner sc = new Scanner(System.in);
		int cycles = sc.nextInt();
		for (int i = 1; i <= cycles; i++) {
			if (HALTFLAG == true) {
				break;
			}
			cyclenumber ++;
			//			System.out.println("Cycle Number:" + i);

			Commit();
			WriteBackALU();
			WriteBackLSFU();
			WriteBackMUL();
			ExecuteMul();
			ExecuteLSFU2();
			ExecuteLSFU1();
			ExecuteAlu2();
			ExecuteAlu1();
			Branch();
			Issue();
			Decode2();
			Decode1();
			FetchStage();

		}
	}

	public static void printMapTables() {
		// Prints rename table
		Iterator<Entry<String, RenameTable>> it2 = renameTable.entrySet().iterator();
		System.out.println("\nRename Table:");
		while (it2.hasNext()) {
			System.out.println(it2.next());
		}
		System.out.println();

		// Prints R-RAT
		System.out.println("\nR-RAT:");
		Iterator<Entry<String, RenameTable>> it3 = r_rat.entrySet().iterator();
		while (it3.hasNext()) {
			System.out.println(it3.next());
		}
		System.out.println();
	}

	public static void printIQ() {
		// Print issue queue
		System.out.println("\nIssue Queue:");
		for (int i = 0; i < issueQueue.size(); i++) {
			if (issueQueue.get(i) != null)
				System.out.println(issueQueue.get(i).toString());
		}
		System.out.println();
	}

	public static void printROB() {
		// Print ROB
		ROB[] rob_array = ROB.getQ();
		System.out.println("ROB:");
		for (int i = 0; i < rob_array.length; i++) {
			if (rob_array[i] != null)
				System.out.println(rob_array[i].toString());
		}
		System.out.println();
	}

	public static void printURF() {
		int i = 0;

		// Print URF
		Iterator<Entry<String, Integer>> it1 = unifiedRegisterFile.entrySet().iterator();
		System.out.println("URF:");
		while (it1.hasNext()) {
			System.out.print(it1.next() + "\t");
			i++;
			if (i == 16)
				System.out.println();
		}
		System.out.println();
	}

	public static void printMemory() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the starting value of memory location to display");
		int i = sc.nextInt();
		System.out.println("Enter the ending value of memory location to display");
		int j = sc.nextInt();

		for (int x = i; x <= j; x++) {
			System.out.println("Memory Value At " + x + " : " + memory[x]);
		}
		System.out.println();
	}

	public static void printPipeline() {

		// Print pipeline
		System.out.println("\nPipeline:");
		Iterator<Entry<String, Instructions>> it4 = pipeline.entrySet().iterator();
		while (it4.hasNext()) {
			Entry<String, Instructions> temp = it4.next();
			System.out.println(temp);
		}
	}

	public static void FetchStage() {
		if (BranchTaken == true) {
			pipeline.put(fetch, null);
			BranchTaken = false;

		} else if (isStall == false && HALTFLAG == false) {

			String ins = InstructionMap.get(programCounter);
			Instructions instruction = new Instructions();
			instruction = instruction.ProcessInstruction(ins, programCounter);
			if (instruction != null) {
				if (instruction.opcode.equals("BNZ") || instruction.opcode.equals("BZ")) {
					BranchPcValue = programCounter;
				}
				if (instruction.opcode.equals("BAL")) {
					NextInstructionBAL = programCounter + 4;
				}
				pipeline.put(fetch, instruction);
				programCounter = programCounter + 4;
			}
		}
	}

	public static void Decode1() {
		if (BranchTaken == false && HALTFLAG == false && isStall == false) {

			if (pipeline.get(fetch) != null) {
				pipeline.put(decode1, pipeline.get(fetch));
				pipeline.put(fetch, null);

			}
		}
		if (BranchTaken == true) {
			pipeline.put(decode1, null);
		}
	}

	public static void Decode2() {

		if (pipeline.get(decode1) != null) {
			if (BranchTaken == true) {
				pipeline.put(decode2, null);
			}

			RenameTable rt = new RenameTable();
			if (BranchTaken == false && HALTFLAG == false) {

				if (isStall == false)

				{
					Instructions ins = pipeline.get(decode1);
					IQ issue = new IQ();
					ROB rob = new ROB();

					switch (ins.opcode) {
					case "ADD":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {

							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							if (renameTable.get(ins.destRegister) == null
									|| renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "SUB":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							if (renameTable.get(ins.destRegister) == null
									|| renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/

								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "MUL":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							if (renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}
								 */
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 2;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "AND":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							if (renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/

								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "OR":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							if (renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/

								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "EX-OR":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							// Renaming Logic
							if (renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/

								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "MOVC":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							// Renaming Logic
							if (renameTable.get(ins.destRegister) == null
									|| renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/

								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							issue.literal = ins.literal;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "LOAD":
						if (!allocationList.isEmpty() && issueQueue.size() != 12 && ROB.size() != 40) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}

							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;

							// Renaming Logic
							if (renameTable.get(ins.destRegister) == null
									|| renameTable.get(ins.destRegister).physicalRegister == null) {
								rt.physicalRegister = allocationList.poll().toString();
								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							} else {

								rt.physicalRegister = allocationList.poll().toString();

								allocationList.add(renameTable.get(ins.destRegister).physicalRegister);
								/*String[] stringArray = Arrays.copyOf(allocationList.toArray(),
										allocationList.toArray().length, String[].class);
								allocationList.clear();
								int[] array = new int[stringArray.length];
								int index = 0;
								for (String str : stringArray) {
									array[index++] = Integer.parseInt(str.substring(1));
								}

								Arrays.sort(array);

								for (int i : array) {
									allocationList.add("P" + i);
								}*/

								rt.valid = false;
								renameTable.put(ins.destRegister, rt);
							}

							// issue queue processing
							issue.fuType = 3;
							issue.destination = rt.physicalRegister;
							ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							issue.src2Valid = true;
							issue.literal = ins.literal;
							putInQ = issue;

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = ins.pc_value;
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "STORE":
						if (issueQueue.size() != 12) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							// issue queue processing
							issue.fuType = 3;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issue.literal = ins.literal;

							// ROB Setup
							rob.memoryAddressForStore = 0;
							rob.isValid = true;
							rob.pc = ins.pc_value;
							ROB.add(rob);

							putInQ = issue;

						} else {
							isStall = true;
						}
						break;
					case "BZ":
						if (issueQueue.size() != 12) {
							branchStall = true;
							// issue queue processing
							issue.src1Valid = true;
							issue.src2Valid = true;
							issue.fuType = 4;
							issue.ins = ins;

							// ROB Setup
							rob.isValid = true;
							rob.pc = ins.pc_value;
							ROB.add(rob);

							String ins2 = InstructionMap.get(ins.pc_value - 4);
							for (int n = 8; n < 8000; n = n + 4) {
								if (ins2.startsWith("STORE"))
									ins2 = InstructionMap.get(ins.pc_value - n);
								else
									break;
							}
							ins2 = ins2.replace(",", " ");
							insbranch = ins2;

							putInQ = issue;

						} else {
							isStall = true;
						}
						break;
					case "BNZ":
						if (issueQueue.size() != 12) {
							branchStall = true;
							// issue queue processing
							issue.fuType = 4;
							issue.src1Valid = true;
							issue.src2Valid = true;
							issue.ins = ins;

							// ROB setup
							rob.isValid = true;
							rob.pc = ins.pc_value;
							ROB.add(rob);

							String ins2 = InstructionMap.get(ins.pc_value - 4);
							for (int n = 8; n < 8000; n = n + 4) {
								if (ins2.startsWith("STORE"))
									ins2 = InstructionMap.get(ins.pc_value - n);
								else
									break;
							}
							ins2 = ins2.replace(",", " ");
							insbranch = ins2;

							putInQ = issue;

						} else {
							isStall = true;
						}
						break;
					case "JUMP":
						if (issueQueue.size() != 12) {
							//branchStall = true;
							// issue queue processing
							issue.src1Valid = true;
							issue.src2Valid = true;
							issue.fuType = 4;
							issue.ins = ins;

							// Rob Setup
							rob.pc = ins.pc_value;
							rob.isValid = true;
							ROB.add(rob);

							/*String ins2 = InstructionMap.get(ins.pc_value - 4);
							for (int n = 8; n < 8000; n = n + 4) {
								if (ins2.startsWith("STORE"))
									ins2 = InstructionMap.get(ins.pc_value - n);
								else
									break;
							}
							ins2 = ins2.replace(",", " ");
							insbranch = ins2;*/

							putInQ = issue;
						} else {
							isStall = true;
						}
						break;
					case "BAL":
						if (issueQueue.size() != 12) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							// issue queue processing
							issue.fuType = 4;
							issue.src2Valid = true;
							issue.ins = ins;
							rob.isValid = true;
							rob.pc = ins.pc_value;
							ROB.add(rob);
							putInQ = issue;
						} else {
							isStall = true;
						}
						break;
					case "HALT":
						if (issueQueue.size() != 12) {

							// issue queue processing
							issue.src1Valid = true;
							issue.src2Valid = true;
							issue.fuType = 4;
							issue.ins = ins;

							// ROB Setup
							rob.pc = ins.pc_value;
							rob.isValid = true;
							ROB.add(rob);

							putInQ = issue;
						} else {
							isStall = true;
						}
						break;
					}
					pipeline.put(decode2, ins);
					pipeline.put(decode1, null);
				}

			}

		}
	}

	public static void Issue() {
		if (BranchTaken == false && isStall == false) {
			if (pipeline.get(decode2) != null) {
				issueQueue.add(putInQ);
				pipeline.put(issueQ, pipeline.get(decode2));
				pipeline.put(decode2, null);
				putInQ = null;
			}
		}
		if (BranchTaken == true)
			pipeline.put(issueQ, null);

	}

	public static void Branch() {
		if (branchStall == false) {
			isStall = false;
			if (!issueQueue.isEmpty()) {
				IQ iq;
				index = issueQueue.size();
				for (int i = 0; i <= index; i++) {
					iq = issueQueue.get(index - 1);
					if (iq != null && iq.fuType == 4 && iq.src1Valid == true && iq.src2Valid == true) {
						issueQueue.remove(index - 1);

						switch (iq.ins.opcode) {
						case "BZ":
							if (ZeroFlag == 1) {
								// Instruction flushed at fetch and decode
								int offset = iq.ins.literal;
								int pcValueForBranch = BranchPcValue;
								programCounter = pcValueForBranch + offset;
								BranchPcValue = 0;
								BranchTaken = true;
								updateROB(0, iq.ins, true);

							}
							break;
						case "BNZ":
							if (ZeroFlag != 1) {
								// Instruction flushed at fetch and decode
								int offset = iq.ins.literal;
								int pcValueForBranch = BranchPcValue;
								programCounter = pcValueForBranch + offset;
								BranchPcValue = 0;
								BranchTaken = true;
								updateROB(0, iq.ins, true);

							}
							break;
						case "JUMP":
							// Flush out the Instruction in fetch and decode
							int registerValue = 0;
							registerValue = unifiedRegisterFile.get(iq.ins.src1Register);

							programCounter = registerValue + iq.ins.literal;
							BranchTaken = true;
							updateROB(0, iq.ins, true);

							break;
						case "BAL":
							int Value = 0;
							unifiedRegisterFile.put("X", NextInstructionBAL);
							Value = iq.valuesrc1;
							programCounter = Value + iq.ins.literal;
							BranchTaken = true;
							updateROB(0, iq.ins, true);

							break;
						case "HALT":
							updateROB(0, iq.ins, true);


							break;
						}
						pipeline.put(issueQ, null);
						pipeline.put(branchALU, iq.ins);
						break;
					} else
						index--;

				}
			}
		} else {
			isStall = true;
		}
	}

	public static void ExecuteAlu1() {

		if (!issueQueue.isEmpty()) {
			IQ iq;
			index = issueQueue.size();
			for (int i = 0; i <= index; i++) {
				iq = issueQueue.get(index - 1);
				if (iq != null && iq.fuType == 1 && !iq.ins.opcode.equals("MOVC") && iq.src1Valid == true
						&& iq.src2Valid == true) {
					switch (iq.ins.opcode) {
					case "ADD":
					case "SUB":
					case "AND":
					case "OR":
					case "EX-OR":
						source1ALU = iq.valuesrc1;
						source2ALU = iq.valuesrc2;
						pipeline.put(execute1, iq.ins);
						issueQueue.remove(index - 1);
						pipeline.put(issueQ, null);
						break;
					}
					break;
				} else if (iq != null && iq.fuType == 1 && iq.ins.opcode.equals("MOVC")) {
					source1ALU = iq.literal;
					pipeline.put(execute1, iq.ins);
					issueQueue.remove(index - 1);
					pipeline.put(issueQ, null);
					break;
				} else
					index--;

			}
		}
	}

	public static void ExecuteAlu2() {
		if (pipeline.get(execute1) != null) {
			Instructions ins = pipeline.get(execute1);
			if (insbranch != null) {
				if (ins.instructionString.equals(insbranch))
					branchStall = false;
			}
			String opcode = ins.opcode;
			int result;

			switch (opcode) {
			case "ADD":
				result = source1ALU + source2ALU;
				if (result == 0 && ins.pc_value + 4 == BranchPcValue) {
					ZeroFlag = 1;
				} else {
					ZeroFlag = 0;
				}

				ins.result = result;
				updateIQ(result, ins);
				updateROB(result, ins, false);

				source1ALU = 0;
				source2ALU = 0;
				break;
			case "SUB":
				result = source1ALU - source2ALU;
				if (result == 0 && ins.pc_value + 4 == BranchPcValue) {
					ZeroFlag = 1;
				} else {
					ZeroFlag = 0;
				}
				source1ALU = 0;
				source2ALU = 0;
				ins.result = result;
				updateIQ(result, ins);
				updateROB(result, ins, false);

				break;
			case "AND":
				result = source1ALU & source2ALU;
				ins.result = result;
				source1ALU = 0;
				source2ALU = 0;
				updateIQ(result, ins);
				updateROB(result, ins, false);

				break;
			case "OR":
				result = source1ALU | source2ALU;
				ins.result = result;
				source1ALU = 0;
				source2ALU = 0;
				updateIQ(result, ins);
				updateROB(result, ins, false);

				break;
			case "EX-OR":
				result = source1ALU ^ source2ALU;
				ins.result = result;
				source1ALU = 0;
				source2ALU = 0;
				updateIQ(result, ins);
				updateROB(result, ins, false);

				break;
			case "MOVC":
				result = literal_zero + source1ALU;
				ins.result = result;
				source1ALU = 0;
				source2ALU = 0;
				updateIQ(result, ins);
				updateROB(result, ins, false);

				break;
			}

			pipeline.put(execute2, ins);
			pipeline.put(execute1, null);
		}
	}

	public static void ExecuteLSFU1() {
		if (!issueQueue.isEmpty()) {
			IQ iq;
			index = issueQueue.size();
			for (int i = 0; i <= index; i++) {
				iq = issueQueue.get(index - 1);
				if (iq != null && iq.fuType == 3 && iq.src1Valid == true && iq.src2Valid == true) {
					switch (iq.ins.opcode) {
					case "LOAD":
						source1LSFU = iq.valuesrc1;
						source2LSFU = iq.literal;
						resultLSFU1 = source1LSFU + source2LSFU;
						pipeline.put(LSFU1, iq.ins);
						pipeline.put(issueQ, null);
						issueQueue.remove(index - 1);
						break;
					case "STORE":
						source1LSFU = iq.valuesrc1;
						source2LSFU = iq.valuesrc2;
						resultLSFU1 = source2LSFU + iq.literal;
						pipeline.put(LSFU1, iq.ins);
						pipeline.put(issueQ, null);
						issueQueue.remove(index - 1);
						break;
					}
					break;
				} else
					index--;
			}
		}

	}

	public static void ExecuteLSFU2() {
		if (loadStoreRobHead && pipeline.get(LSFU1) != null) {
			Instructions ins = pipeline.get(LSFU1);
			loadStoreRobHead = false;
			String opcode = ins.opcode;
			int memoryResult;
			switch (opcode) {
			case "LOAD":
				memoryResult = memory[resultLSFU1];

				ins.result = memoryResult;
				updateIQ(memoryResult, ins);

				source1LSFU = 0;
				source2LSFU = 0;
				resultLSFU1 = 0;

				updateROB(memoryResult, ins, false);
				break;
			case "STORE":
				ins.result = source1LSFU;

				memory[resultLSFU1] = ins.result;

				resultLSFU1 = 0;
				source1LSFU = 0;
				source2LSFU = 0;
				storeCommited++;
				numberofInsprocessed++;
				ROB.remove();
				break;
			}
			pipeline.put(LSFU2, ins);
			pipeline.put(LSFU1, null);
		}
	}

	public static void ExecuteMul() {
		if (!issueQueue.isEmpty() && counter == 4) {
			IQ iq;
			index = issueQueue.size();
			for (int i = 0; i <= index; i++) {
				iq = issueQueue.get(index - 1);
				if (iq != null && iq.fuType == 2 && iq.src1Valid == true && iq.src2Valid == true) {
					mulIQ = iq;
					issueQueue.remove(index - 1);

					pipeline.put(issueQ, null);
					pipeline.put(multiply, iq.ins);
					break;
				} else
					index--;
			}
		}

		if (pipeline.get(multiply) != null && counter != 0)
			counter--;

		else if (pipeline.get(multiply) != null && counter == 0) {
			// decrementing counter further for the MUL WB to pick up the
			// instruction in next cycle
			counter--;
			IQ iq = mulIQ;
			mulIQ = null;
			// iq.ins = pipeline.get(multiply);
			// iq.setIns(pipeline.get(multiply));
			source1MUL = iq.valuesrc1;
			source2MUL = iq.valuesrc2;
			iq.ins.result = source1MUL * source2MUL;
			if (iq.ins.result == 0 && iq.ins.pc_value + 4 == BranchPcValue) {
				ZeroFlag = 1;
			} else {
				ZeroFlag = 0;
			}
			source1MUL = 0;
			source2MUL = 0;
			updateIQ(iq.ins.result, iq.ins);
			updateROB(iq.ins.result, iq.ins, false);

		}
	}

	public static void WriteBackMUL() {
		if (pipeline.get(multiply) != null && counter == -1) {
			counter = 4;
			Instructions ins = pipeline.get(multiply);
			// forwarding from wb
			updateIQ(ins.result, ins);
			pipeline.put(writeMultiply, ins);
			pipeline.put(multiply, null);
			unifiedRegisterFile.put(ins.physicalDestRegister, ins.result);
			renameTable.get(ins.destRegister).valid = true;
		}

	}

	public static void WriteBackLSFU() {
		if (pipeline.get(LSFU2) != null) {
			Instructions ins = pipeline.get(LSFU2);
			if (ins.opcode.equalsIgnoreCase("LOAD")) {
				// forwarding from wb
				updateIQ(ins.result, ins);
				unifiedRegisterFile.put(ins.destRegister, ins.result);
				renameTable.get(ins.destRegister).valid = true;
			}
			pipeline.put(writeLSFU, ins);
			pipeline.put(LSFU2, null);
		}
	}

	public static void WriteBackALU() {
		if (pipeline.get(execute2) != null) {
			Instructions ins = pipeline.get(execute2);
			// forwarding from wb
			updateIQ(ins.result, ins);
			unifiedRegisterFile.put(ins.physicalDestRegister, ins.result);
			renameTable.get(ins.destRegister).valid = true;
			pipeline.put(writeALU, ins);
			pipeline.put(execute2, null);
		}
	}

	public static void Commit() {
		pipeline.put(branchALU, null);
		pipeline.put(writeALU, null);
		pipeline.put(writeMultiply, null);
		pipeline.put(writeLSFU, null);
		ROB rob_array[] = null;
		rob_array = ROB.getQ();
		ROB rob = new ROB();
		rob = rob_array[ROB.getHeadIndex()];

		if (rob != null) {
			String ins = InstructionMap.get(rob.pc);
			Instructions instruction = new Instructions();
			instruction = instruction.ProcessInstruction(ins, rob.pc);
			
			if (instruction.opcode.equalsIgnoreCase("STORE")) {
				loadStoreRobHead = true;
			} else if (instruction.opcode.equalsIgnoreCase("LOAD")) {
				loadStoreRobHead = true;
				RenameTable rt = new RenameTable();
				if (rob.isValid && !rob.isBranchTaken) {
					unifiedRegisterFile.put(rob.destinationRegsiter, rob.value);
					rt.physicalRegister = rob.physicalRegister;
					rt.valid = true;
					r_rat.put(rob.destinationRegsiter, rt);
					loadCommited++;
					numberofInsprocessed++;
					ROB.remove();
				}
			} else if (instruction.opcode.equalsIgnoreCase("HALT")) {
				numberofInsprocessed++;
				HALTFLAG = true;
				initializePipeline();
				for (int i = 0; i < rob_array.length; i++) 
				{
					if (rob_array[i] != null)
						ROB.remove();
				}
				if(!ROB.isEmpty())
				ROB.remove();
			} else {
				RenameTable rt = new RenameTable();
				if (rob != null && rob.isValid && !rob.isBranchTaken && !instruction.opcode.equalsIgnoreCase("BNZ")
						&& !instruction.opcode.equalsIgnoreCase("BZ") && !instruction.opcode.equalsIgnoreCase("BAL")
						&& !instruction.opcode.equalsIgnoreCase("JUMP")) {
					unifiedRegisterFile.put(rob.destinationRegsiter, rob.value);
					rt.physicalRegister = rob.physicalRegister;
					rt.valid = true;
					r_rat.put(rob.destinationRegsiter, rt);
					numberofInsprocessed++;
					ROB.remove();
				} else if (rob != null && rob.isValid && !rob.isBranchTaken
						&& (instruction.opcode.equalsIgnoreCase("BNZ") || instruction.opcode.equalsIgnoreCase("BZ")
								|| instruction.opcode.equalsIgnoreCase("BAL")
								|| instruction.opcode.equalsIgnoreCase("JUMP"))) {
					numberofInsprocessed++;
					ROB.remove();
				} else if (rob != null && rob.isValid && rob.isBranchTaken) {
					BranchTaken = false;
					renameTable.clear();
					Iterator<Entry<String, RenameTable>> it = r_rat.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, RenameTable> temp = it.next();
						RenameTable _rt = new RenameTable();
						_rt = temp.getValue();
						String tempStr = new String();
						tempStr = temp.getKey();
						renameTable.put(tempStr, _rt);
					}
					allocationList.clear();
					for (int n = 0; n < sizeUrf - 17; n++) {
						allocationList.add("P" + n);
					}
					for (int i = 0; i < renameTable.size(); i++) {
						RenameTable _rt2 = new RenameTable();
						_rt2 = renameTable.get("R" + i);
						if (_rt2 != null)
							allocationList.remove(_rt2.physicalRegister);
					}

					// remove the branch instruction from ROB
					ROB.remove();

					// rolling back ROB for the entries that follow BR if branch
					// is
					// taken
					int robHead = ROB.getHeadIndex();
					for (int i = robHead; i < ROB.size() + robHead; i++) {
						ROB.remove();
					}
				}
			}
		}
	}

	public static void updateROB(int result, Instructions ins, boolean isBranchTaken) {
		ROB rob_array[] = null;
		rob_array = ROB.getQ();
		// int headIndex = ROB.getHeadIndex();
		for (int i = 0; i < rob_array.length; i++) {
			if (rob_array[i] != null && rob_array[i].pc == ins.pc_value) {
				rob_array[i].value = ins.result;
				rob_array[i].isValid = true;
				rob_array[i].isBranchTaken = isBranchTaken;
				if (ins.opcode.equals("STORE")) {
					rob_array[i].memoryAddressForStore = result;
				}
				break;
			}
		}
		ROB.setQ(rob_array);
	}

	public static void updateIQ(int result, Instructions ins) {
		for (int i = 0; i < issueQueue.size(); i++) {
			IQ IQins = issueQueue.get(i);
			if (IQins != null) {
				if ((IQins.ins.opcode.equals("MOVC") || IQins.ins.opcode.equals("LOAD"))
						&& IQins.ins.src1Register.equals(ins.destRegister)) {
					IQins.valuesrc1 = result;
					IQins.src1Valid = true;
					issueQueue.set(i, IQins);
					break;
				} else {
					if (IQins.ins.src1Register.equalsIgnoreCase(ins.destRegister)) {
						IQins.valuesrc1 = result;
						IQins.src1Valid = true;
						issueQueue.set(i, IQins);
					}
					if (IQins.ins.src2Register.equalsIgnoreCase(ins.destRegister)) {
						IQins.valuesrc2 = result;
						IQins.src2Valid = true;
						issueQueue.set(i, IQins);
						break;
					}
				}
			}
		}
	}

	public static void changeUrfSize() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the URF  size");
		sizeUrf = sc.nextInt();
		System.out.println("New URF  size set: " + sizeUrf);
	}

	public static void initializePipeline() {
		pipeline.put(fetch, null);
		pipeline.put(decode1, null);
		pipeline.put(decode2, null);
		pipeline.put(issueQ, null);
		pipeline.put(execute1, null);
		pipeline.put(execute2, null);
		pipeline.put(multiply, null);
		pipeline.put(LSFU1, null);
		pipeline.put(LSFU2, null);
		pipeline.put(branchALU, null);
		pipeline.put(writeALU, null);
		pipeline.put(writeMultiply, null);
		pipeline.put(writeLSFU, null);

	}
}