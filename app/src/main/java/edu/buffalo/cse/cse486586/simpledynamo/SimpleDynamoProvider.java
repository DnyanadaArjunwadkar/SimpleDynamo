package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
	static final String REMOTE_PORT0 = "11108";
	static final String REMOTE_PORT1 = "11112";
	int sum=0;
	static final String REMOTE_PORT2 = "11116";
	static final String REMOTE_PORT3 = "11120";
	static final String REMOTE_PORT4 = "11124";
	static final String TAG = SimpleDynamoProvider.class.getSimpleName();
	static int QueryFlag;
	static int portRep1;
	static int portRep2;
	int portPred;
	static  int predFailGlo;
	static int replicaFetch;
	static int predFetch;
	static int noneedQF;
	static int noneedstarQF;
	static int alsort;
	static  int newflag;
	static int nodeRejoin;
	static final int SERVER_PORT = 10000;
	private static final String PROVIDER_NAME = "content://edu.buffalo.cse.cse486586.simpledynamo.provider";
	private static final Uri uri = Uri.parse("content://" + PROVIDER_NAME);
	static ArrayList al= new ArrayList();
	static String queryAns=null;
	static String permissionToProceed=null;
	static int replica1fail=0;
	static int QueryFlagStar=0;
	static List<String> starResults = new ArrayList<String>();
	static String starQueryAns=null;
	static List<String> starShared = new ArrayList<String>();
	MatrixCursor mt = null;
	static int socketreply=0;

	static List<String> repResults = new ArrayList<String>();
	static List<String> predResults = new ArrayList<String>();

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		getContext().deleteFile(selection);
		//******************************************
		Log.e("Delete:", selection);
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public  Uri insert(Uri uri, ContentValues cvalues) {
		// TODO Auto-generated method stub

		//	Log.e("Insert function begins...", cvalues.toString());
		newflag=0;
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		String myPortInsert = String.valueOf((Integer.parseInt(portStr) * 2));
		int mePort = Integer.parseInt(myPortInsert) / 2;
		//Debug pt // I am the target port, store the file
		Log.e("mePort Value is is:", String.valueOf(mePort));

		//Log.e("INSERT THIS:----->", String.valueOf(al));
		String ins = "" + cvalues.get("key");

		String hashedKey = null;
		try {
			hashedKey = genHash(ins);
			Log.e("INSERT:Hashed key", hashedKey);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		al.add(hashedKey);

		//after adding key sort Al

		Collections.sort(al, new HashComp());
		int j, k;
		//Log.e("INSERT:after adding key and sorting al: add+Sortng", String.valueOf(al));

		for (int i = 0; i < al.size(); i++) {

			//determine if it is a Key or a Node
			String temp = (String) al.get(i);
			//		Log.e("Typecast: String from Al:", temp);

			String[] tempArr = temp.split("#");
			if (tempArr.length == 2)//this is a node//do nothing
			{
				//		Log.e("This is a NODE with port from Al", "");
			} else// this is a key//insert it to the next port number from AL//tempArr=Key
			{
				//Log.e("index of key in AL:", String.valueOf(i));
				int flag = 0;
				if (i == al.size() - 1)//c1//key in last idex
				{
					//	Log.e("Thorve c1 i:", String.valueOf(i));
					i = 0;
					j = i + 1;
					k = i + 2;
					flag = 1;
					//Log.e("Thorve c1 flag:", String.valueOf(flag));
					//Log.e("Thorve c1 i:", String.valueOf(i));
					//Log.e("Thorve c1 j:", String.valueOf(j));
					//Log.e("Thorve c1 k:", String.valueOf(k));

				} else {
					i = i + 1;
					if (i == al.size() - 1)//target at last index
					{
						j = 0;
						k = j + 1;
						//	Log.e("Key in 2nd last index i:", String.valueOf(j));
						//	Log.e("Key in 2nd last index j:", String.valueOf(j));
						//	Log.e("key in 2nd last index k:", String.valueOf(k));

					} else if (i == al.size() - 2)//target 2nd last
					{
						j = al.size() - 1;
						k = 0;
						//	Log.e("Key in 3rd last index i:", String.valueOf(j));
						//	Log.e("Key in 3rd last index j:", String.valueOf(j));
						//	Log.e("key in 3rd last index k:", String.valueOf(k));
					} else {
						j = i + 1;
						k = j + 1;
						//Log.e("index i:", String.valueOf(j));
						//	Log.e("index j:", String.valueOf(j));
						///	Log.e("index k:", String.valueOf(k));
					}
					//	Log.e("dny c1 i:", String.valueOf(i));
					flag = 2;
					//	Log.e("dny c1 flag:", String.valueOf(flag));
				}

				//Log.e("for Avd index i:", String.valueOf(i));
				//	Log.e("Outside index j:", String.valueOf(j));
				//	Log.e("Outside index k:", String.valueOf(k));

				String targetAVD = (String) al.get(i);//target avd
				String targetAVDsucc1 = (String) al.get(j);//target avd succ1
				String targetAVDsucc2 = (String) al.get(k);//target avd succ2
				//	Log.e("targetAVD= al.get(i):", String.valueOf(i));
				//	Log.e("targetAVDsucc=al.get(j):", String.valueOf(j));
				//	Log.e("targetAVDsucc2=:al.get(k)", String.valueOf(k));

				//for target avd handle conditions

				if (flag == 1) {
					i = al.size() - 1;//restore i
					//		Log.e("Thorve 2 c1 i:", String.valueOf(i));
					//		Log.e("Thorve c1 flag:", String.valueOf(flag));
				}
				if (flag == 2) {
					i = i - 1;
					//		Log.e("dny 2 c1 i:", String.valueOf(i));
					//		Log.e("dny 2 c1 flag:", String.valueOf(flag));
				}

				//	Log.e("Test tim hortons** targetAVD:", targetAVD);
				//	Log.e("Test tim hortons** targetAVDsucc1-->:", targetAVDsucc1);
				//	Log.e("Test tim hortons** targetAVDsucc2----->:", targetAVDsucc2);

				//it should give you the hashed string of a port number which is next to you in AL
				Log.e("String target avd--> port no:", targetAVD);
				Log.e("targetAVDsucc1------> port no:", targetAVDsucc1);
				Log.e("targetAVDsucc2---------> port no:", targetAVDsucc2);


				String[] tempArr2sukanya = targetAVD.split("#");
				//you get hashed node id and port no in tempArr2[1] where the file will be stored
				//port no:tempArr2[1]
				//Target port now known
				Log.e("i value is:", String.valueOf(i));

				String[] tempArr2Succ1 = targetAVDsucc1.split("#");
				String[] tempArr2Succ2 = targetAVDsucc2.split("#");

				int succ1 = Integer.parseInt(tempArr2Succ1[1]) * 2;
				int succ2 = Integer.parseInt(tempArr2Succ2[1]) * 2;

				//	Log.e("tempArr2sukanya[1].toString():", tempArr2sukanya[1].toString());
				//	Log.e("targetAVDsucc1[].tostring:", targetAVDsucc1.toString());
				//	Log.e("succ1 :", String.valueOf(succ1));
				//	Log.e("targetAVDsucc2[]..tostring :", targetAVDsucc2.toString());
				//	Log.e("succ2 :", String.valueOf(succ2));


				al.remove(i);//remove key from al

				//	Log.e("al.size() after removing key :", String.valueOf(al.size()));

				//	for (int d = 0; d < al.size(); d++) {
				//		Log.e("After Removing key Al element:"+d+":", String.valueOf(al.get(d)));
				//	}

				Log.e("INSERT:Test3 After"," Key Remove");

				Collections.sort(al, new HashComp());
				for (int d = 0; d < al.size(); d++) {
					//	Log.e("Final AL element :" + d + ":", String.valueOf(al.get(d)));
				}

				//	Log.e("Myport is:", String.valueOf(mePort));
				//Debug pt // I am the target port, store the file
				//synchronized(this)
				{
					if (String.valueOf(mePort).equals(tempArr2sukanya[1])) {

						Log.e("mePort=tempArr2sukanya[1]:", "C1");
						Context ctx = getContext();
						String filename = "" + ins;

						String string = cvalues.get("value") + "\n";
						FileOutputStream outputStream;
						try {
							outputStream = ctx.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
							outputStream.write(string.getBytes());
							outputStream.close();
						} catch (Exception e) {
							Log.e("Excpt caught is :", e.toString());
						}

						Log.e("inserting CV:", cvalues.toString());

					}
					if (mePort == Integer.parseInt(tempArr2Succ1[1])) {

						Log.e("C2------------->", "C2");


						Context ctx = getContext();
						String filename = "" + ins;

						String string = cvalues.get("value") + "\n";
						FileOutputStream outputStream;
						try {
							outputStream = ctx.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
							outputStream.write(string.getBytes());
							outputStream.close();
						} catch (Exception e) {
							Log.e("Excpt caught is :", e.toString());
						}

						Log.e("inserting CV:", cvalues.toString());

					}
					if (mePort == (Integer.parseInt(tempArr2Succ2[1]))) {
						Log.e("C3", "C3");

						Context ctx = getContext();
						String filename = "" + ins;

						String string = cvalues.get("value") + "\n";
						FileOutputStream outputStream;
						try {
							outputStream = ctx.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
							outputStream.write(string.getBytes());
							outputStream.close();
						} catch (Exception e) {
							Log.e("Excpt caught is :", e.toString());
						}

						Log.e("inserting CV:", cvalues.toString());

					}


					if (!tempArr2sukanya[1].equals(String.valueOf(mePort)))//socket connection with target and n//dominos
					{
						Log.e("Inserting in target port", "c4");
						String keyString = (String) cvalues.get("key");
						String valString = (String) cvalues.get("value");
						String cKeyVal = "KeyAdd" + "#" + keyString + "#" + valString;
						Socket socketnew = null;
						DataOutputStream dos=null;
						try {
							int target = Integer.parseInt(tempArr2sukanya[1]) * 2;
							socketnew = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(String.valueOf(target)));


							//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), target);
							//	socketnew.connect(sockAddress, 2000);


							dos = new DataOutputStream(socketnew.getOutputStream());

							Log.e("Msg to target avd is:", cKeyVal);
							dos.writeUTF(cKeyVal);

							newflag = 1;

						} catch (IOException e) {
							e.printStackTrace();
						}
						finally {
							try {
								socketnew.close();
								dos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}


						}
						Log.e("c4 : !tempArr2sukanya[1].equals(mePort)********", cKeyVal);

					}


					if (!tempArr2Succ1[1].equals(String.valueOf(mePort)))//socket connection with target and n
					{
						Log.e("Inserting in SUCC 1", "c5");
						String keyString = (String) cvalues.get("key");
						String valString = (String) cvalues.get("value");
						String cKeyVal = "KeyAdd" + "#" + keyString + "#" + valString;
						Socket socketnew1 = null;
						DataOutputStream dos1=null;
						try {
							socketnew1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(String.valueOf(succ1)));


							//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), succ1);
							//socketnew1.connect(sockAddress,2000);

							dos1 = new DataOutputStream(socketnew1.getOutputStream());
							Log.e("Msg to target avd is:", cKeyVal);
							dos1.writeUTF(cKeyVal);
							newflag = 1;
						} catch (IOException e) {
							e.printStackTrace();
						}
						finally{
							try {
								socketnew1.close();
								dos1.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						Log.e("c5 !tempArr2Succ1[1].equals(mePort)********", cKeyVal);

					}

					if (!tempArr2Succ2[1].equals(String.valueOf(mePort)))//socket connection with target and n
					{
						Log.e("Inserting in SUCC 2", "c6");
						String keyString = (String) cvalues.get("key");
						String valString = (String) cvalues.get("value");
						String cKeyVal = "KeyAdd" + "#" + keyString + "#" + valString;
						Socket socketnew2 = null;
						DataOutputStream dos2=null;
						try {
							socketnew2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(String.valueOf(succ2)));

							//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(String.valueOf(succ2)));
							//socketnew2.connect(sockAddress,2000);

							dos2 = new DataOutputStream(socketnew2.getOutputStream());
							Log.e("Msg to target avd is:", cKeyVal);
							dos2.writeUTF(cKeyVal);
							newflag = 1;
						} catch (IOException e) {
							e.printStackTrace();
						}
						finally{

							try {
								socketnew2.close();
								dos2.close();
							} catch (IOException e) {
								e.printStackTrace();
							}


						}

						Log.e("c6 !tempArr2Succ2[1].equals(mePort)********", cKeyVal);

					}
				}


				if (newflag == 1) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}



			}
		}//sync
		//else end
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		Log.e("OnCreate", "yes");

		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		int myPortInt= Integer.parseInt(myPort)/2;
		Log.e("myPortInt", String.valueOf(myPortInt));

		File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/app_Node_dir").listFiles();
		if(files!=null)
		{
			//	Log.e("I dont think", "1st INIT");
			Log.e("File Length",String.valueOf(files.length));
			for (int k = 0; k < files.length; k++)
			{
				if (files[k].isFile())
				{
					Log.e("KEYname of file:Oncreate", files[k].getName());
					if((files[k].getName()).equals(myPort))
					{
						Log.e("NODE REJOIN", "NODE REJOIN");
						nodeRejoin=1;
					}
				}
			}
		}
		else
		{
			Log.e("it is 1st time init", "First Instance");
			{
				Log.e("onCreate dny","Else");
				Context ctx = getContext();
				//	String filename = myPort;
				String content = "This is the text content";
				File Node = ctx.getDir("Node_dir", Context.MODE_PRIVATE); //Creating an internal dir;
				File fileWithinMyDir = new File(Node, myPort); //Getting a file within the dir.
				try
				{
					FileOutputStream out = new FileOutputStream(fileWithinMyDir);
					//out.write();
					if (!fileWithinMyDir.exists())
					{
						try {
							fileWithinMyDir.createNewFile();
							byte[] contentInBytes = content.getBytes();
							out.write(contentInBytes);
							out.flush();
							out.close();
							Log.e("Done","written");
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					Log.e("File ", "created");
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}

		{
			try
			{
				ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
				new ServerTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, serverSocket);
			}
			catch (SocketTimeoutException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		String p0 = "5554";
		try {
			String node_id_0 = genHash(p0);
			node_id_0 = node_id_0 + "#" + "5554";
			al.add(node_id_0);
			//Log.e("dht provider node_id_0:", node_id_0);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String p1 = "5556";
		try {
			String node_id_1 = genHash(p1);
			node_id_1 = node_id_1 + "#" + "5556";
			al.add(node_id_1);
			//	Log.e("dht provider node_id_1:", node_id_1);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String p2 = "5558";
		try {
			String node_id_2 = genHash(p2);
			node_id_2 = node_id_2 + "#" + "5558";
			al.add(node_id_2);
			//	Log.e("dht provider node_id_2:", node_id_2);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String p3 = "5560";
		try {
			String node_id_3 = genHash(p3);
			node_id_3 = node_id_3 + "#" + "5560";
			al.add(node_id_3);
			//	Log.e("dht provider node_id_3:", node_id_3);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String p4 = "5562";
		try {
			String node_id_4 = genHash(p4);
			node_id_4 = node_id_4 + "#" + "5562";
			al.add(node_id_4);
			//	Log.e("dht provider node_id_4:", node_id_4);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Collections.sort(al, new HashComp());
		alsort=1;

		//Log.e("After Arraylist is sorted in onCreate :", "");
		for (int i = 0; i < al.size(); i++) {

			//Log.e("Arraylist element " + i + ":", String.valueOf(al.get(i)));
		}



		if (nodeRejoin==1 && alsort==1) //determine predessor and replica for connection
		{
			Log.e("NodeRejoin==1", "has begun");
			int j;
			int predessor;
			for (int i = 0; i < al.size(); i++)
			{
				String t= (String) al.get(i);
				String[] tarr=t.split("#");
				if(myPortInt==Integer.parseInt(tarr[1])) {

					//	Log.e("In for loop:tarr1", tarr[1]);
					int flag = 0;
					if(i==0)
					{

						predessor=al.size()-1;
						//	Log.e("Thorve i==0 Predessor:", String.valueOf(predessor));
					}
					else if(i == al.size() - 1)
					{
						predessor=i-1;
						//		Log.e("Thorve c1 Predessor:", String.valueOf(predessor));
					}
					else {
						predessor = i - 1;
						//	Log.e("Thorve Normal Predessor:", String.valueOf(predessor));
					}
					if (i == al.size() - 1)//c1//key in last idex
					{
						i = 0;
						j = i + 1;
						//k = i + 2;
						flag = 1;
					}
					else
					{
						i = i + 1;
						if (i == al.size() - 1)//target at last index
						{
							j = 0;
						}
						else if (i == al.size() - 2)//target 2nd last
						{
							j = al.size() - 1;
						}
						else
						{
							j = i + 1;
						}
						flag = 2;

					}
					String rep1 = (String) al.get(i);//target avd
					String rep2 = (String) al.get(j);//target avd succ1
					String pred=(String) al.get(predessor);

					//	Log.e("String REPLICA 1:", rep1);
					//	Log.e("String REPLICA 2:", rep2);
					//	Log.e("Predessor name:", pred);

					String[] rep1arr = rep1.split("#");
					String[] rep2arr = rep2.split("#");
					String[] predArr = pred.split("#");

					portRep1 = Integer.parseInt(rep1arr[1]) * 2;
					portRep2 = Integer.parseInt(rep2arr[1]) * 2;
					portPred = Integer.parseInt(predArr[1]) * 2;

					Log.e("rep1 :", String.valueOf(portRep1));
					Log.e("rep2 :", String.valueOf(portRep2));
					Log.e("Predessor :", String.valueOf(portPred));

					//determine previous port

					if (flag == 1) {
						i = al.size() - 1;//restore i
					}
					if (flag == 2) {
						i = i - 1;
					}
					{
						//String message="Rejoin"+"#"+myPort+"#"+portRep1+"#"+portRep2;//myPort=11108

						final String msg="Rejoin"+"#"+myPort;
						final String msgPred="PredRejoin"+"#"+myPort;

						Log.e("myPort->"+myPortInt+"<--->"+portRep1/2+"msg:", msg);
						//	Log.e("replication --->:", msg);
						Log.e("myPort->"+myPortInt+"<--->"+portPred/2+"msgPred:", msgPred);
						//Log.e("Predessor --->:", msgPred);


						Thread thread = new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								Socket socketR = new Socket();
								DataOutputStream dos11;
								DataInputStream din;
								try
								{
									//Your code goes here
//comehere

									socketR = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											Integer.parseInt(String.valueOf(portRep1)));
									//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), (portRep1));
									//socketR.connect(sockAddress, 100);
												socketR.setSoTimeout(2000);
									din=new DataInputStream(socketR.getInputStream());
									//	Log.e("c", "cccccccc");
									String aru=din.readUTF();
									//	Log.e("aru", aru);
									if(!aru.isEmpty() || aru!=null || !aru.equals(""))
									{
										//		Log.e("rcvd str aru ->", aru);
										//Log.e("Msg to target avd is:", cKeyVal);
										dos11 = new DataOutputStream(socketR.getOutputStream());
										Log.e("CTCT replica 1:", msg);
										dos11.writeUTF(msg);

									}
									else
									{
										replica1fail = 1;
										Log.e("\treplica1fail = 1",String.valueOf(replica1fail));
									}

								}
								catch (SocketTimeoutException e) {
									Log.e("Replica 1", "SocketTimeoutException");
									replica1fail=1;
									e.printStackTrace();
									// Node Failure

								} catch (EOFException e) {
									Log.e("Replica 1", "EOFException");
									replica1fail=1;
									e.printStackTrace();
									// Node Failure

								} catch (SocketException e) {
									Log.e("Replica 1", "EOFException");
									replica1fail=1;
									e.printStackTrace();
									// Node Failure

								} catch (Exception e) {
									Log.e("Replica 1", "Exception");
									replica1fail=1;
									e.printStackTrace();
								}
								finally
								{
									try {
										socketR.close();//dos11.close();
									} catch (IOException e) {
										e.printStackTrace();
									}


								}

								if (replica1fail==1)//socket connection with target and n
								{
									Log.e("Replica1 has failed.try", "Replica 2");
									//Socket socketR2 = null;

									try {

										Socket socketR1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												portRep2);
										DataOutputStream dos1 = new DataOutputStream(socketR1.getOutputStream());
										Log.e("CTCT replica 2:", msg);
										dos1.writeUTF(msg);
										socketR1.close();

									} catch (SocketTimeoutException e) {
										Log.e("Replica 2", "SocketTimeoutException");
										replica1fail=2;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("Replica 2", "EOFException");
										replica1fail=2;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("Replica 2", "EOFException");
										replica1fail=2;
										e.printStackTrace();
										// Node Failure

									} catch (Exception e) {
										Log.e("Replica 2", "Exception");
										replica1fail=2;
										e.printStackTrace();
									}
								}

								if(replica1fail==2)
								{
									Log.e("Replica 2 has failed.", "Take action now");
								}

							}
						});

						Thread thread2 = new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								Socket socketR1=new Socket();
								DataOutputStream dos2;
								DataInputStream din;
								try
								{
									//Your code goes here

									socketR1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											Integer.parseInt(String.valueOf(portPred)));
									//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), (portPred));
									//socketR1.connect(sockAddress, 100);
									socketR1.setSoTimeout(2000);


									din=new DataInputStream(socketR1.getInputStream());
									//	Log.e("c", "cccccccc");
									String aru=din.readUTF();
									//	Log.e("aru", aru);
									if(!aru.isEmpty() || aru!=null || !aru.equals(""))
									{
										//		Log.e("rcvd str aru ->", aru);
										//Log.e("Msg to target avd is:", cKeyVal);
										dos2 = new DataOutputStream(socketR1.getOutputStream());
										Log.e("CTCT replica 1:", msgPred);
										dos2.writeUTF(msgPred);

									}
									else
									{
										predFailGlo = 1;
										Log.e("\tpredFailGlo = 1",String.valueOf(predFailGlo));
									}

								}
								catch (SocketTimeoutException e) {
									Log.e("Predessor 1", "SocketTimeoutException");
									predFailGlo=1;
									e.printStackTrace();
									// Node Failure

								} catch (EOFException e) {
									Log.e("Predessor 1", "EOFException");
									//	replica1fail=1;
									predFailGlo=1;
									e.printStackTrace();
									// Node Failure

								} catch (SocketException e) {
									Log.e("Predessor 1", "EOFException");
									//replica1fail=1;
									predFailGlo=1;
									e.printStackTrace();
									// Node Failure

								} catch (Exception e) {
									Log.e("Predessor 1", "Exception");
									//replica1fail=1;
									predFailGlo=1;
									e.printStackTrace();
								}
								finally{

									try {
										socketR1.close();
										//dos2.close();
									} catch (IOException e) {
										e.printStackTrace();
									}


								}


								if(predFailGlo==1)
								{
									int preponc=0;
									if(portPred==11124)
									{
										preponc=11120;
									}
									if(portPred==11112)
									{
										preponc=11124;
									}
									if(portPred==11108)
									{
										preponc=11112;
									}
									if(portPred==11116)
									{
										preponc=11108;
									}
									if(portPred==11120)
									{
										preponc=11116;
									}

									Socket socketR12=new Socket();
									DataOutputStream dos3;
									DataInputStream din2;
									try
									{
										socketR12 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												Integer.parseInt(String.valueOf(preponc)));
										socketR12.setSoTimeout(2000);
										//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), (preponc));
										//socketR12.connect(sockAddress, 100);


										din2=new DataInputStream(socketR12.getInputStream());
										//	Log.e("c", "cccccccc");
										String aru=din2.readUTF();
										//	Log.e("aru", aru);
										if(!aru.isEmpty() || aru!=null || !aru.equals(""))
										{
											//		Log.e("rcvd str aru ->", aru);
											//Log.e("Msg to target avd is:", cKeyVal);
											dos3 = new DataOutputStream(socketR12.getOutputStream());
											Log.e("CTCT Pred->Pred->pred 1", msgPred);
											dos3.writeUTF(msgPred);

										}
										else
										{
											predFailGlo = 1;
											Log.e("\tpredFailGlo = 1",String.valueOf(predFailGlo));
										}

									}
									catch (SocketTimeoutException e) {
										Log.e("Pred->Pred->pred", "SocketTimeoutException");
										//	predFailGlo=1;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("Pred->Pred->pred", "EOFException");
										//	replica1fail=1;
										//predFailGlo=1;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("Pred->Pred->pred", "EOFException");
										//replica1fail=1;
										//predFailGlo=1;
										e.printStackTrace();
										// Node Failure

									} catch (Exception e) {
										Log.e("Pred->Pred->pred", "Exception");
										//replica1fail=1;
										//	predFailGlo=1;
										e.printStackTrace();
									}
									finally {
										try {
											socketR12.close(); //close resources here!
											//dos3.close();
										} catch (IOException e) {
											e.printStackTrace();
										}

									}
								}
							}

						});

						thread.start();
						Log.e("PREDRESULTS before--[]-->flags", String.valueOf(predResults));
						Log.e("Replica RESULTS before--[]-->flags", String.valueOf(repResults));

						Log.e("Replicafetch *WHILE* B4---->", String.valueOf(replicaFetch));
						newflag=1;
						while(replicaFetch==0 )
						{

						}
						Log.e("Replicafetch *WHILE* aftr--->", String.valueOf(replicaFetch));

						if(replicaFetch==1)
						{
							String keyname=null;
							String valinkey=null;
							Log.e("replica RESULTS *WHILE* aftr--->", String.valueOf(repResults));
							Log.e("Do local insert here file and value--->", String.valueOf(repResults));
							if(repResults!=null) {
								for (int a = 0; a < repResults.size(); a++) {
									String gaga = repResults.get(a);
									String[] gagaArr = gaga.split("#");
									if(gagaArr.length>1)
									{
										keyname = gagaArr[0];
										valinkey = gagaArr[1];
									}

									//Log.e("Gaga keyname:" + gagaArr[0] + ":valinkey:" + gagaArr[1] + ":gaga:", gaga);

									Context ctx = getContext();
									String filename = "" + keyname;

									String string = valinkey + "\n";
									FileOutputStream outputStream;
									try {
										outputStream = ctx.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
										outputStream.write(string.getBytes());
										outputStream.close();
									} catch (Exception e) {
										Log.e("Excpt caught is :", e.toString());
									}
									//Log.e("gaga key value inserted", "success");
								}
							}
							thread2.start();
						}
						Log.e("PREDfetch *WHILE* B4---->", String.valueOf(predFetch));
						newflag=1;
						while(predFetch==0 )
						{

						}
						Log.e("PREDfetch *WHILE* aftr--->", String.valueOf(predFetch));

						if(predFetch==1) {
							Log.e("PREDRESULTS *WHILE* aftr--->", String.valueOf(predResults));
							Log.e("Do local insert here file and value-PRed->", String.valueOf(predResults));
							String keyname = null;
							String valinkey = null;
							if (predResults != null)
							{
								for (int b = 0; b < predResults.size(); b++) {
									String sia = predResults.get(b);
									String[] siaArr = sia.split("#");
									if(siaArr.length>1)
									{
										keyname = siaArr[0];
										valinkey = siaArr[1];
									}

									//Log.e("sia keyname:"+siaArr[0]+":valinkey:"+siaArr[1]+":sia:", sia);

									Context ctx = getContext();
									String filename = "" + keyname;

									String string = valinkey + "\n";
									FileOutputStream outputStream;
									try {
										outputStream = ctx.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
										outputStream.write(string.getBytes());
										outputStream.close();
									} catch (Exception e) {
										Log.e("Excpt caught is :", e.toString());
									}
									//	Log.e("sia key value inserted", "success");
								}
							}
						}
					}
				}//if

			}//for

		}//if nodeRejoin==1

		return true;
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
									 String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		noneedQF=0;
		sum=0;
		Log.e("Entered in Q Selection->:", selection);
		//	Log.e("status of Al in Query ", al.toString());
		Context ctx1 = getContext();

		String valInKey = "";

		List<String> results = new ArrayList<String>();
		String[] column = new String[]{"key", "value"};
		mt = new MatrixCursor(column);

		Log.e("Given Query:", selection);
		if (selection.equals("*"))
		{
			{
				TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
				String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
				final String myPortInsert = String.valueOf((Integer.parseInt(portStr) * 2));
				int mePort = Integer.parseInt(myPortInsert) / 2;
				MatrixCursor m1 = new MatrixCursor(column);
				Log.e("Query function myPrtinsert:", myPortInsert);

				int SmallFlag=0;
				//--------------------------------------------------------------------------------------------------------------------------------------------

				if(al.contains("177ccecaec32c54b82d5aaafc18a2dadb753e3b1#5562") )
				{
					Log.e("al.contains 5562", "");
					if(myPortInsert.equals("11124"))
					{
						Log.e("11124==myPrtinsert:", myPortInsert);

						File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
						if(files!=null)
						{
							for (int k = 0; k < files.length; k++) {
								if (files[k].isFile()) {
									results.add(files[k].getName());

									//Log.e("KEYname of file:", files[k].getName());
								}
							}
						}
						//d	Log.e("list of files:results:", results.toString());
						//d Log.e("in *size of results:", String.valueOf(results.size()));

						File f = new File(getContext().getFilesDir().getAbsolutePath());
						for (int c = 0; c < results.size(); c++) {
							StringBuffer sb = new StringBuffer();
							FileInputStream fis = null;
							try {
								fis = ctx1.getApplicationContext().openFileInput(results.get(c));
								BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
								while ((valInKey = bis.readLine()) != null) {
									sb.append(valInKey);
									//d			Log.e("in *print c", String.valueOf(c));
									//d			Log.e("File f:", f.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							String valuetobereturned = sb.toString();
							//	Log.e("append(value2breturnd):", valuetobereturned);

							String row[] = new String[]{results.get(c), valuetobereturned};
							mt.addRow(row);
							//noneedstarQF=1;
							//d	Log.e("mt.row:***:", String.valueOf(row));
							//d	Log.e("int C ***:", results.get(c) + "*" + c);
							//d	Log.e("results.toString ***", results.toString());
							//d	Log.e("mt ****", mt.toString());
						}

					}
					else {
						//d	Log.e("Query: I am Port:" + myPortInsert, "");
						Log.e("Query: Before Star Query Flag:(0)", String.valueOf(QueryFlagStar));

						String cKeyVal = "StarKeyQuery" + "#" + selection + "#" + mePort;

						Socket socketQ = new Socket();
						DataInputStream din;
						DataOutputStream  dos;
						//	DataOutputStream dos=null;
						int failAvd4 = 0; int failAvd4Succ1 = 0;
						try {
							//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT4));
							//	socketQ.connect(sockAddress,2000);
//gradwalk
							socketQ = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(REMOTE_PORT4));
							socketQ.setSoTimeout(2000);

							// = new DataOutputStream(socketQ.getOutputStream());
							din=new DataInputStream(socketQ.getInputStream());
							//	Log.e("c", "cccccccc");
							String aru=din.readUTF();
							//	Log.e("aru", aru);
							if(!aru.isEmpty() || aru!=null || !aru.equals(""))
							{
								//		Log.e("rcvd str aru ->", aru);
								//Log.e("Msg to target avd is:", cKeyVal);
								dos = new DataOutputStream(socketQ.getOutputStream());
								Log.e("STAR: Msg to avd4 avd is:"+REMOTE_PORT4, cKeyVal);
								dos.writeUTF(cKeyVal);

								dos.writeUTF(cKeyVal);
							}
							else
							{
								failAvd4 = 1;
								Log.e("aru:failAvd4 = 1.",String.valueOf(failAvd4));
							}

						}
						catch (SocketTimeoutException e) {
							Log.e("*Q:SOCKET CONN WITH AVD4 FAILED", "SockeTimeout SUCC 1");
							failAvd4 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (EOFException e) {
							Log.e("*Q:SOCKET CONN WITH AVD4 FAILED", "EOFException SUCC 1");
							failAvd4 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (SocketException e) {

							Log.e("*Q:SOCKET CONN WITH AVD4 FAILED", "SocketException SUCC 1");
							failAvd4 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (IOException e) {
							Log.e("*Q:SOCKET CONN WITH AVD4 FAILED", "IOException SUCC 1");
							failAvd4 = 1;
							e.printStackTrace();
						} catch (Exception e) {

							Log.e("*Q:SOCKET CONN WITH AVD4 FAILED",  "Exception SUCC 1");
							failAvd4 = 1;
							e.printStackTrace();
						}
						finally{

							try {
								socketQ.close();
								//dos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						if(failAvd4==1)
						{
							{
								if (mePort==5556) {
									{
										///locally fetch
										Log.e("*Q:Find key inside me:SUCC1==meport:", String.valueOf(mePort));

										File f = new File(getContext().getFilesDir().getAbsolutePath());
										FileInputStream fis = null;
										StringBuffer sb = new StringBuffer();
										try {
											fis = ctx1.getApplicationContext().openFileInput(selection);
											Log.e("Query File f:", f.toString());
											BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
											while ((valInKey = bis.readLine()) != null)
											{
												sb.append(valInKey);
											}

										}
										catch (FileNotFoundException e)
										{
											e.printStackTrace();
										}
										catch (IOException e)
										{
											e.printStackTrace();
										}
										String valuetobereturned = sb.toString();

										//	mt = new MatrixCursor(column);
										String row[] = new String[]{selection, valuetobereturned};
										mt.addRow(row);
										//dny
										noneedstarQF=1;
									}
								} else {
									//form socket connection with succ1
									Log.e("SockConnFlag==1", "RemoteQuery");
									Socket sockets1 = null;
									try {
										sockets1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												Integer.parseInt(REMOTE_PORT1));
										DataOutputStream dos1 = new DataOutputStream(sockets1.getOutputStream());
										Log.e("*Q:Msg sent to SUCC 1 is:", cKeyVal);
										//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
										dos1.writeUTF(cKeyVal);
									}catch (SocketTimeoutException e) {
										Log.e("*Query:failAvd4->Succ1", "SockeTimeout");
										failAvd4Succ1 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("*Query:failAvd->4Succ1", "EOFException");
										failAvd4Succ1 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("*Query:failAvd4->Succ1", "SocketException");
										failAvd4Succ1 = 2;

										e.printStackTrace();
										// Node Failure

									} catch (IOException e) {
										Log.e("*Query:failAvd4->Succ1", "IOException");
										failAvd4Succ1 = 2;
										e.printStackTrace();
									} catch (Exception e) {
										Log.e("*Query:failAvd4->Succ1", "Exception");
										failAvd4Succ1 = 2;
										e.printStackTrace();
									}

								}
							}

						}

						if(noneedstarQF!=1) {
							Log.e("*Q: Before *While* QueryFlagStar:", String.valueOf(QueryFlagStar));

							while (QueryFlagStar == 0) {

							}
							Log.e("*Q:After *WHile* QueryFlagStar:", String.valueOf(QueryFlagStar));
							Log.e("STAR Query:After WHile starShared:-------->", String.valueOf(starShared));

							if (starShared.size() != 0) {
								Log.e("Final StarShared:size:", String.valueOf(starShared.size()));
								Log.e("Myport --------->:", myPortInsert);
								for (int d = 0; d < starShared.size(); d++) {

									String UB = String.valueOf(starShared.get(d));

									if (UB != null) {
										String[] UBcse = UB.split("#");
										if (UBcse.length > 1) {
											//d					Log.e("UB Key-value from Final starShared:", UB);
											String selection1 = UBcse[0];
											//d			Log.e("UB Key ------------------>:", UBcse[0]);
											String valuetobereturned = UBcse[1];
											//d		Log.e("UB valuetobereturned ------------------>:", UBcse[1]);

											String row[] = new String[]{selection1, valuetobereturned};
											mt.addRow(row);
										}
									}
								}
							}
							Log.e("5562 size", String.valueOf(starShared.size()));
							QueryFlagStar = 0;
							//if(QueryFlag!=0)
							//{
							//    Log.e("Query: QueryAns", queryAns);
							//"KeyQuery key word ahe ignore kar" + "#" + KeyQuery+"#"+myPortInsert;
							//queryAns=lastArr[2]+"#"+lastArr[2];//selection+#+valu

							String[] tempQans = queryAns.split("#");
							String selection1 = tempQans[0];
							String valuetobereturned = tempQans[1];
							Log.e("End of qurrying 5562; QueryFlagStar=", String.valueOf(QueryFlagStar));
							// Log.e("Query: ans)", String.valueOf(queryAns));
							// mt = new MatrixCursor(column);
							//}
							//   Log.e("Query: After  if(QueryFlag!=0)", String.valueOf(QueryFlag));
							starShared.clear();
						}
					}
				}

//--------------------------------------------------------------------------------------------------------------------------------------------

				if(al.contains("33d6357cfaaf0f72991b0ecd8c56da066613c089#5554"))
				{
					int failAvd0=0;
					int noneedQF0=0;
					Log.e("al.contains 5554", "yo");
					if(myPortInsert.equals("11108"))

					{
						Log.e("11108==myPrtinsert:", myPortInsert);

						File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
						if(files!=null) {
							for (int k = 0; k < files.length; k++) {
								if (files[k].isFile()) {
									results.add(files[k].getName());

									Log.e("KEYname of file:", files[k].getName());
								}
							}
						}

						//d		Log.e("list of files:results:", results.toString());
						//d	Log.e("in *size of results:", String.valueOf(results.size()));

						File f = new File(getContext().getFilesDir().getAbsolutePath());
						for (int c = 0; c < results.size(); c++) {
							StringBuffer sb = new StringBuffer();
							FileInputStream fis = null;
							try {
								fis = ctx1.getApplicationContext().openFileInput(results.get(c));
								BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
								while ((valInKey = bis.readLine()) != null) {
									sb.append(valInKey);
									//d				Log.e("in *print c", String.valueOf(c));
									//d				Log.e("File f:", f.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							String valuetobereturned = sb.toString();
							//d	Log.e("append(value2breturnd):", valuetobereturned);

							String row[] = new String[]{results.get(c), valuetobereturned};
							mt.addRow(row);
							noneedQF0=1;

							//d		Log.e("mt.row:***:", String.valueOf(row));
							//d	Log.e("int C ***:", results.get(c) + "*" + c);
							//d	Log.e("results.toString ***", results.toString());
							//d	Log.e("mt ****", mt.toString());
						}

					}
					else {
						//d	Log.e("Myport ------------->:", myPortInsert);
						Log.e("Query: I am Port:" + myPortInsert, "");
						Log.e("Query: Before Star Query Flag:(0)", String.valueOf(QueryFlagStar));

						String cKeyVal = "StarKeyQuery" + "#" + selection + "#" + mePort;

						Socket socketQ = new Socket();
						DataOutputStream dos;
						DataInputStream din;
						try {

							socketQ = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(REMOTE_PORT0));
							socketQ.setSoTimeout(2000);
							//SocketAddress sockAddress = new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT0));
							//
							// socketQ.connect(sockAddress, 2000);


							//dance
							din=new DataInputStream(socketQ.getInputStream());
							Log.e("c", "cccccccc");
							String aru=din.readUTF();
							Log.e("aru", aru);
							if(!aru.isEmpty() || aru!=null || !aru.equals(""))
							{
								Log.e("rcvd str aru ->", aru);
								//Log.e("Msg to target avd is:", cKeyVal);
								dos = new DataOutputStream(socketQ.getOutputStream());
								Log.e("STAR: Msg to avd0 avd is:"+REMOTE_PORT0, cKeyVal);
								dos.writeUTF(cKeyVal);

							}
							else
							{
								failAvd0 = 1;
								Log.e("aru:failAvd0 = 1.",String.valueOf(failAvd0));
							}

						} catch (SocketTimeoutException e) {
							Log.e("*Q:SOCKET CONN WITH AVD0 FAILED", "SockeTimeout SUCC 1");
							failAvd0 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (EOFException e) {
							Log.e("*Q:SOCKET CONN WITH AVD0 FAILED", "EOFException SUCC 1");
							failAvd0 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (SocketException e) {

							Log.e("*Q:SOCKET CONN WITH AVD0 FAILED", "SocketException SUCC 1");
							failAvd0 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (IOException e) {
							Log.e("*Q:SOCKET CONN WITH AVD0 FAILED", "IOException SUCC 1");
							failAvd0 = 1;
							e.printStackTrace();
						} catch (Exception e) {

							Log.e("*Q:SOCKET CONN WITH AVD0 FAILED", "Exception SUCC 1");
							failAvd0 = 1;
							e.printStackTrace();
						}
						finally{
							try {
								socketQ.close();
								//dos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
						{
							if (failAvd0 == 1) {
								if (mePort == 5558) {
									{
										///locally fetch
										Log.e("*Q:Find key inside me:SUCC1==meport:", String.valueOf(mePort));

										File f = new File(getContext().getFilesDir().getAbsolutePath());
										FileInputStream fis = null;
										StringBuffer sb = new StringBuffer();
										try {
											fis = ctx1.getApplicationContext().openFileInput(selection);
											Log.e("Query File f:", f.toString());
											BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
											while ((valInKey = bis.readLine()) != null) {
												sb.append(valInKey);
											}

										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										}
										String valuetobereturned = sb.toString();

										//	mt = new MatrixCursor(column);
										String row[] = new String[]{selection, valuetobereturned};
										mt.addRow(row);
										//dny
										noneedQF0 = 1;
									}
								} else {
									//form socket connection with succ1
									Log.e("SockConnFlag==1", "RemoteQuery");
									Socket sockets1 = null;
									try {
										sockets1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												Integer.parseInt(REMOTE_PORT2));
										DataOutputStream dos1 = new DataOutputStream(sockets1.getOutputStream());
										Log.e("*Q:Msg sent to SUCC 1 is:", cKeyVal);
										//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
										dos1.writeUTF(cKeyVal);
									} catch (SocketTimeoutException e) {
										Log.e("*Query:failAvd0->Succ1", "SockeTimeout");
										failAvd0 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("*Query:failAvd0->Succ1", "EOFException");
										failAvd0 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("*Query:failAvd0->Succ1", "SocketException");
										failAvd0 = 2;

										e.printStackTrace();
										// Node Failure

									} catch (IOException e) {
										Log.e("*Query:failAvd0->Succ1", "IOException");
										failAvd0 = 2;
										e.printStackTrace();
									} catch (Exception e) {
										Log.e("*Query:failAvd0->Succ1", "Exception");
										failAvd0 = 2;
										e.printStackTrace();
									}

								}
							}

						}
						if(noneedQF0!=1)
						{
							Log.e("*Q: Before *While* QueryFlagStar:", String.valueOf(QueryFlagStar));

							while (QueryFlagStar == 0) {

							}
							Log.e("*Q:After *WHile* QueryFlagStar:QueryFlagsStar=", String.valueOf(QueryFlagStar));
							Log.e("STAR Query:After WHile starShared:-------->", String.valueOf(starShared));

							if (starShared.size() != 0) {
								Log.e("Myport ------------------>:", myPortInsert);
								Log.e("Final StarShared:size", String.valueOf(starShared.size()));

								for (int d = 0; d < starShared.size(); d++) {
									Log.e("Final StarShared:" + d + ":", String.valueOf(starShared.get(d)));

									String UB = String.valueOf(starShared.get(d));
									if (UB != null) {
										String[] UBcse = UB.split("#");
										if (UBcse.length > 1) {
											//d					Log.e("UB Key-value from Final starShared:", UB);
											String selection1 = UBcse[0];
											//d					Log.e("UB Key ------------------>:", UBcse[0]);
											String valuetobereturned = UBcse[1];
											//d					Log.e("UB valuetobereturned ------------------>:", UBcse[1]);

											String row[] = new String[]{selection1, valuetobereturned};
											mt.addRow(row);
										}
									}

								}
							}
							//d	Log.e("mt size", String.valueOf(starShared.size()));
							QueryFlagStar = 0;
							//if(QueryFlag!=0)
							//{
							//    Log.e("Query: QueryAns", queryAns);
							//"KeyQuery key word ahe ignore kar" + "#" + KeyQuery+"#"+myPortInsert;
							//queryAns=lastArr[2]+"#"+lastArr[2];//selection+#+valu

							String[] tempQans = queryAns.split("#");
							String selection1 = tempQans[0];
							String valuetobereturned = tempQans[1];
							Log.e("End of Qurying 5554:QueryFlagStar=", String.valueOf(QueryFlagStar));
							// Log.e("Query: ans)", String.valueOf(queryAns));
							// mt = new MatrixCursor(column);

							//}
							//   Log.e("Query: After  if(QueryFlag!=0)", String.valueOf(QueryFlag));
							starShared.clear();
						}
					}
				}

//--------------------------------------------------------------------------------------------------------------------------------------------


				if(al.contains("208f7f72b198dadd244e61801abe1ec3a4857bc9#5556"))
				{
					int noneedofQF1=0;
					int failAvd1=0;
					Log.e("al.contains 5556", "yes");
					if(myPortInsert.equals("11112"))
					{

						Log.e("11112==myPrtinsert:", myPortInsert);

						File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
						if(files!=null) {
							for (int k = 0; k < files.length; k++) {
								if (files[k].isFile()) {
									results.add(files[k].getName());

									Log.e("KEYname of file:", files[k].getName());
								}
							}
						}
						Log.e("list of files:results:", results.toString());
						//d	Log.e("in *size of results:", String.valueOf(results.size()));

						File f = new File(getContext().getFilesDir().getAbsolutePath());
						for (int c = 0; c < results.size(); c++) {
							StringBuffer sb = new StringBuffer();
							FileInputStream fis = null;
							try {
								fis = ctx1.getApplicationContext().openFileInput(results.get(c));
								BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
								while ((valInKey = bis.readLine()) != null) {
									sb.append(valInKey);
									//d						Log.e("in *print c", String.valueOf(c));
									//d						Log.e("File f:", f.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							String valuetobereturned = sb.toString();
							//d		Log.e("append(value2breturnd):", valuetobereturned);

							String row[] = new String[]{results.get(c), valuetobereturned};
							mt.addRow(row);
							//d		Log.e("mt.row:***:", String.valueOf(row));
							//d		Log.e("int C ***:", results.get(c) + "*" + c);
							//d		Log.e("results.toString ***", results.toString());
							//d		Log.e("mt ****", mt.toString());
						}

					}
					else {
						Log.e("Query: I am Port:" + myPortInsert, "");
						Log.e("Query: Before Star Query Flag:(0)", String.valueOf(QueryFlagStar));

						String cKeyVal = "StarKeyQuery" + "#" + selection + "#" + mePort;

						Socket socketQ = new Socket();
						DataOutputStream dos;
						DataInputStream din;
						try {
							socketQ = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(REMOTE_PORT1));
							socketQ.setSoTimeout(2000);
							//SocketAddress sockAddress = new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT1));
							//socketQ.connect(sockAddress, 2000);


							din=new DataInputStream(socketQ.getInputStream());
							Log.e("c", "cccccccc");
							String aru=din.readUTF();
							Log.e("aru", aru);
							if(!aru.isEmpty() || aru!=null || !aru.equals(""))
							{
								Log.e("rcvd str aru ->", aru);
								//Log.e("Msg to target avd is:", cKeyVal);
								dos = new DataOutputStream(socketQ.getOutputStream());
								Log.e("STAR: Msg to avd1 avd is:"+REMOTE_PORT1, cKeyVal);
								dos.writeUTF(cKeyVal);

							}
							else
							{
								failAvd1 = 1;
								Log.e("aru:failAvd0 = 1.",String.valueOf(failAvd1));
							}

						} catch (SocketTimeoutException e) {
							Log.e("*Q:SOCKET CONN WITH AVD1 FAILED", "SockeTimeout SUCC 1");
							failAvd1 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (EOFException e) {
							Log.e("*Q:SOCKET CONN WITH AVD1 FAILED", "EOFException SUCC 1");
							failAvd1 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (SocketException e) {

							Log.e("*Q:SOCKET CONN WITH AVD1 FAILED", "SocketException SUCC 1");
							failAvd1 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (IOException e) {
							Log.e("*Q:SOCKET CONN WITH AVD1 FAILED", "IOException SUCC 1");
							failAvd1 = 1;
							e.printStackTrace();
						} catch (Exception e) {

							Log.e("*Q:SOCKET CONN WITH AVD1 FAILED", "Exception SUCC 1");
							failAvd1 = 1;
							e.printStackTrace();
						}
						finally {
							try {
								socketQ.close();
								//dos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
						if (failAvd1 == 1) {


							{
								if (mePort == 5554) {
									{
										///locally fetch
										Log.e("*Q:Find key inside me:SUCC1==meport:", String.valueOf(mePort));

										File f = new File(getContext().getFilesDir().getAbsolutePath());
										FileInputStream fis = null;
										StringBuffer sb = new StringBuffer();
										try {
											fis = ctx1.getApplicationContext().openFileInput(selection);
											Log.e("Query File f:", f.toString());
											BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
											while ((valInKey = bis.readLine()) != null) {
												sb.append(valInKey);
											}

										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										}
										String valuetobereturned = sb.toString();

										//	mt = new MatrixCursor(column);
										String row[] = new String[]{selection, valuetobereturned};
										mt.addRow(row);
										//dny
										noneedofQF1 = 1;
									}
								} else {
									//form socket connection with succ1
									Log.e("SockConnFlag==1", "RemoteQuery");
									Socket sockets1 = null;
									try {
										sockets1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												Integer.parseInt(REMOTE_PORT0));
										DataOutputStream dos1 = new DataOutputStream(sockets1.getOutputStream());
										Log.e("*Q:Msg sent to SUCC 1 is:", cKeyVal);
										//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
										dos1.writeUTF(cKeyVal);
									} catch (SocketTimeoutException e) {
										Log.e("*Query:failAvd1->Succ1", "SockeTimeout");
										failAvd1 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("*Query:failAvd1->Succ1", "EOFException");
										failAvd1 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("*Query:failAvd4Succ1", "SocketException");
										failAvd1 = 2;

										e.printStackTrace();
										// Node Failure

									} catch (IOException e) {
										Log.e("*Query:failAvd1->Succ1", "IOException");
										failAvd1 = 2;
										e.printStackTrace();
									} catch (Exception e) {
										Log.e("*Query:failAvd1->Succ1", "Exception");
										failAvd1 = 2;
										e.printStackTrace();
									}
								}
							}
						}//end of failAvd1
						if (noneedofQF1 != 1)
						{
							Log.e("*Q: Before *While* QueryFlagStar:", String.valueOf(QueryFlagStar));

							while (QueryFlagStar == 0) {

							}

							Log.e("*Q:After *WHile* QueryFlagStar:", String.valueOf(QueryFlagStar));
							Log.e("STAR Query:After WHile starShared:-------->", String.valueOf(starShared));
							if (starShared.size() != 0) {

								Log.e("Myport ------------>:", myPortInsert);
								Log.e("Final StarShared:size", String.valueOf(starShared.size()));
								for (int d = 0; d < starShared.size(); d++) {

									String UB = String.valueOf(starShared.get(d));

									if (UB != null) {
										String[] UBcse = UB.split("#");
										if (UBcse.length > 1) {
											//d			Log.e("UB Key-value from Final starShared:", UB);
											String selection1 = UBcse[0];
											//d			Log.e("UB Key ------------------>:", UBcse[0]);
											String valuetobereturned = UBcse[1];
											//d			Log.e("UB valuetobereturned ------------------>:", UBcse[1]);

											String row[] = new String[]{selection1, valuetobereturned};
											mt.addRow(row);
										}
									}
								}
							}
							//dLog.e("mt size", String.valueOf(starShared.size()));
							QueryFlagStar = 0;
							//if(QueryFlag!=0)
							//{
							//    Log.e("Query: QueryAns", queryAns);
							//"KeyQuery key word ahe ignore kar" + "#" + KeyQuery+"#"+myPortInsert;
							//queryAns=lastArr[2]+"#"+lastArr[2];//selection+#+valu

							String[] tempQans = queryAns.split("#");
							String selection1 = tempQans[0];
							String valuetobereturned = tempQans[1];
							Log.e("End of Qurrying 5556:QueryFlagStar=", String.valueOf(QueryFlagStar));
							// Log.e("Query: ans)", String.valueOf(queryAns));
							// mt = new MatrixCursor(column);

							//}
							//   Log.e("Query: After  if(QueryFlag!=0)", String.valueOf(QueryFlag));
							starShared.clear();
						}
					}
				}

//--------------------------------------------------------------------------------------------------------------------------------------------


				if(al.contains("abf0fd8db03e5ecb199a9b82929e9db79b909643#5558"))
				{
					int failAvd2=0;
					int noneedofQF2=0;
					Log.e("al.contains 5558", "");
					if(myPortInsert.equals("11116"))
					{
						Log.e("11116==myPrtinsert:", myPortInsert);

						File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
						if(files!=null) {
							for (int k = 0; k < files.length; k++) {
								if (files[k].isFile()) {
									results.add(files[k].getName());

									//Log.e("KEYname of file:", files[k].getName());
								}
							}
						}
						//d	Log.e("list of files:results:", results.toString());
						//d	Log.e("in *size of results:", String.valueOf(results.size()));

						File f = new File(getContext().getFilesDir().getAbsolutePath());
						for (int c = 0; c < results.size(); c++) {
							StringBuffer sb = new StringBuffer();
							FileInputStream fis = null;
							try {
								fis = ctx1.getApplicationContext().openFileInput(results.get(c));
								BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
								while ((valInKey = bis.readLine()) != null) {
									sb.append(valInKey);
									Log.e("in *print c", String.valueOf(c));
									Log.e("File f:", f.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							String valuetobereturned = sb.toString();
							Log.e("append(value2breturnd):", valuetobereturned);

							String row[] = new String[]{results.get(c), valuetobereturned};
							mt.addRow(row);
							//d		Log.e("mt.row:***:", String.valueOf(row));
							//d			Log.e("int C ***:", results.get(c) + "*" + c);
							//d			Log.e("results.toString ***", results.toString());
							//d			Log.e("mt ****", mt.toString());
						}

					}
					else {
						Log.e("Query: I am Port:" + myPortInsert, "");
						Log.e("Query: Before Star Query Flag:(0)", String.valueOf(QueryFlagStar));
						String cKeyVal = "StarKeyQuery" + "#" + selection + "#" + mePort;

						Socket socketQ = new Socket();
						DataOutputStream dos;
						DataInputStream din;
						try {

							socketQ = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(REMOTE_PORT2));
							socketQ.setSoTimeout(2000);
							//SocketAddress sockAddress = new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT2));
							//socketQ.connect(sockAddress, 2000);

							din=new DataInputStream(socketQ.getInputStream());
							Log.e("c", "cccccccc");
							String aru=din.readUTF();
							Log.e("aru", aru);
							if(!aru.isEmpty() || aru!=null || !aru.equals(""))
							{
								Log.e("rcvd str aru ->", aru);
								//Log.e("Msg to target avd is:", cKeyVal);
								dos = new DataOutputStream(socketQ.getOutputStream());
								Log.e("STAR: Msg to avd2 avd is:"+REMOTE_PORT2, cKeyVal);
								dos.writeUTF(cKeyVal);

							}
							else
							{
								failAvd2 = 1;
								Log.e("aru:failAvd0 = 1.",String.valueOf(failAvd2));
							}

							dos = new DataOutputStream(socketQ.getOutputStream());
							Log.e("STAR: Msg to target avd is:" + REMOTE_PORT2, cKeyVal);
							dos.writeUTF(cKeyVal);

						} catch (SocketTimeoutException e) {
							Log.e("*Q:SOCKET CONN WITH AVD2 FAILED", "SockeTimeout SUCC 1");
							failAvd2 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (EOFException e) {
							Log.e("*Q:SOCKET CONN WITH AVD2 FAILED", "EOFException SUCC 1");
							failAvd2 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (SocketException e) {

							Log.e("*Q:SOCKET CONN WITH AVD2 FAILED", "SocketException SUCC 1");
							failAvd2 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (IOException e) {
							Log.e("*Q:SOCKET CONN WITH AVD2 FAILED", "IOException SUCC 1");
							failAvd2 = 1;
							e.printStackTrace();
						} catch (Exception e) {

							Log.e("*Q:SOCKET CONN WITH AVD2 FAILED", "Exception SUCC 1");
							failAvd2 = 1;
							e.printStackTrace();
						}
						finally {
							try {
								socketQ.close();
								//dos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
						if (failAvd2 == 1) {
							{
								if (mePort == 5560) {
									{
										///locally fetch
										Log.e("*Q:Find key inside me:SUCC1==meport:", String.valueOf(mePort));

										File f = new File(getContext().getFilesDir().getAbsolutePath());
										FileInputStream fis = null;
										StringBuffer sb = new StringBuffer();
										try {
											fis = ctx1.getApplicationContext().openFileInput(selection);
											Log.e("Query File f:", f.toString());
											BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
											while ((valInKey = bis.readLine()) != null) {
												sb.append(valInKey);
											}

										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										}
										String valuetobereturned = sb.toString();

										//	mt = new MatrixCursor(column);
										String row[] = new String[]{selection, valuetobereturned};
										mt.addRow(row);
										//dny
										noneedofQF2 = 1;
									}
								} else {
									//form socket connection with succ1
									Log.e("SockConnFlag==1", "RemoteQuery");
									Socket sockets1 = null;
									try {
										sockets1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												Integer.parseInt(REMOTE_PORT3));
										DataOutputStream dos1 = new DataOutputStream(sockets1.getOutputStream());
										Log.e("*Q:Msg sent to SUCC 1 is:", cKeyVal);
										//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
										dos1.writeUTF(cKeyVal);
									} catch (SocketTimeoutException e) {
										Log.e("*Query:failAvd2->Succ1", "SockeTimeout");
										failAvd2 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("*Query:failAvd2->Succ1", "EOFException");
										failAvd2 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("*Query:failAvd2->Succ1", "SocketException");
										failAvd2 = 2;

										e.printStackTrace();
										// Node Failure

									} catch (IOException e) {
										Log.e("*Query:failAvd2->Succ1", "IOException");
										failAvd2 = 2;
										e.printStackTrace();
									} catch (Exception e) {
										Log.e("*Query:failAvd2->Succ1", "Exception");
										failAvd2 = 2;
										e.printStackTrace();
									}
								}
							}

						}//end of fail avd2
						if(noneedofQF2!=1)
						{
							Log.e("*Q: Before *While* QueryFlagStar:", String.valueOf(QueryFlagStar));

							while (QueryFlagStar == 0) {

							}
							Log.e("*Q:After *WHile* QueryFlagStar:", String.valueOf(QueryFlagStar));
							Log.e("STAR Query:After WHile starShared:-------->", String.valueOf(starShared));

							if (starShared.size() != 0) {
								Log.e("Myport ----------->:", myPortInsert);
								Log.e("Final StarShared:size", String.valueOf(starShared.size()));
								for (int d = 0; d < starShared.size(); d++) {
									Log.e("Final StarShared:" + d + ":", String.valueOf(starShared.get(d)));

									String UB = String.valueOf(starShared.get(d));
									String[] UBcse = UB.split("#");
									if (UBcse.length > 1) {
										//d				Log.e("UB Key-value from Final starShared:", UB);
										String selection1 = UBcse[0];
										//d Log.e("UB Key ------------------>:", UBcse[0]);
										String valuetobereturned = UBcse[1];
										//d Log.e("UB valuetobereturned ------------------>:", UBcse[1]);

										String row[] = new String[]{selection1, valuetobereturned};
										mt.addRow(row);
									}

								}
							}
							Log.e("mt size", String.valueOf(starShared.size()));
							QueryFlagStar = 0;
							//if(QueryFlag!=0)
							//{
							//    Log.e("Query: QueryAns", queryAns);
							//"KeyQuery key word ahe ignore kar" + "#" + KeyQuery+"#"+myPortInsert;
							//queryAns=lastArr[2]+"#"+lastArr[2];//selection+#+valu

							String[] tempQans = queryAns.split("#");
							String selection1 = tempQans[0];
							String valuetobereturned = tempQans[1];
							Log.e("End of Qurrying 5558:QueryFlagStar=", String.valueOf(QueryFlagStar));
							// Log.e("Query: ans)", String.valueOf(queryAns));
							// mt = new MatrixCursor(column);

							//}
							//   Log.e("Query: After  if(QueryFlag!=0)", String.valueOf(QueryFlag));
							starShared.clear();
						}//end of noneed
					}
				}

				//---------------------------------------------------------------------------------------------------------------------------------


				if(al.contains("c25ddd596aa7c81fa12378fa725f706d54325d12#5560")) {
					Log.e("al.contains 5560", "");
					int failAvd3=0;
					int noneedofQF3=0;
					if (myPortInsert.equals("11120"))
					{
						Log.e("11124==myPrtinsert:", myPortInsert);

						File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
						if(files!=null) {
							for (int k = 0; k < files.length; k++) {
								if (files[k].isFile()) {
									results.add(files[k].getName());

									Log.e("KEYname of file:", files[k].getName());
								}
							}
						}
						//d		Log.e("list of files:results:", results.toString());
						//d		Log.e("in *size of results:", String.valueOf(results.size()));

						File f = new File(getContext().getFilesDir().getAbsolutePath());
						for (int c = 0; c < results.size(); c++) {
							StringBuffer sb = new StringBuffer();
							FileInputStream fis = null;
							try {
								fis = ctx1.getApplicationContext().openFileInput(results.get(c));
								BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
								while ((valInKey = bis.readLine()) != null) {
									sb.append(valInKey);
									Log.e("in *print c", String.valueOf(c));
									Log.e("File f:", f.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							String valuetobereturned = sb.toString();
							Log.e("append(value2breturnd):", valuetobereturned);

							String row[] = new String[]{results.get(c), valuetobereturned};
							mt.addRow(row);
							//d				Log.e("mt.row:***:", String.valueOf(row));
							//d				Log.e("int C ***:", results.get(c) + "*" + c);
							//d				Log.e("results.toString ***", results.toString());
							//d				Log.e("mt ****", mt.toString());
						}



					} else {

						Log.e("Query: I am Port:" + myPortInsert, "");
						Log.e("Query: Before Star Query Flag:(0)", String.valueOf(QueryFlagStar));
						String cKeyVal = "StarKeyQuery" + "#" + selection + "#" + mePort;

						Socket socketQ = new Socket();
						DataOutputStream dos;
						DataInputStream din;

						try {
							socketQ = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(REMOTE_PORT3));

							//SocketAddress sockAddress = new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT3));
							//socketQ.connect(sockAddress, 2000);



							din=new DataInputStream(socketQ.getInputStream());
							Log.e("c", "cccccccc");
							String aru=din.readUTF();
							Log.e("aru", aru);
							if(!aru.isEmpty() || aru!=null || !aru.equals(""))
							{
								Log.e("rcvd str aru ->", aru);
								//Log.e("Msg to target avd is:", cKeyVal);
								dos = new DataOutputStream(socketQ.getOutputStream());
								Log.e("STAR: Msg to avd3 avd is:"+REMOTE_PORT3, cKeyVal);
								dos.writeUTF(cKeyVal);

							}
							else
							{
								failAvd3 = 1;
								Log.e("aru:failAvd0 = 1.",String.valueOf(failAvd3));
							}


						} catch (SocketTimeoutException e) {
							Log.e("*Q:SOCKET CONN WITH AVD3 FAILED", "SockeTimeout SUCC 1");
							failAvd3 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (EOFException e) {
							Log.e("*Q:SOCKET CONN WITH AVD3 FAILED", "EOFException SUCC 1");
							failAvd3 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (SocketException e) {

							Log.e("*Q:SOCKET CONN WITH AVD3 FAILED", "SocketException SUCC 1");
							failAvd3 = 1;
							e.printStackTrace();
							// Node Failure

						} catch (IOException e) {
							Log.e("*Q:SOCKET CONN WITH AVD3 FAILED", "IOException SUCC 1");
							failAvd3 = 1;
							e.printStackTrace();
						} catch (Exception e) {

							Log.e("*Q:SOCKET CONN WITH AVD3 FAILED", "Exception SUCC 1");
							failAvd3 = 1;
							e.printStackTrace();
						}
						finally {
							try {
								socketQ.close();
								//dos.close();

							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						if (failAvd3 == 1) {
							{
								if (mePort == 5562) {
									{
										///locally fetch
										Log.e("*Q:Find key inside me:SUCC1==meport:", String.valueOf(mePort));

										File f = new File(getContext().getFilesDir().getAbsolutePath());
										FileInputStream fis = null;
										StringBuffer sb = new StringBuffer();
										try {
											fis = ctx1.getApplicationContext().openFileInput(selection);
											Log.e("Query File f:", f.toString());
											BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
											while ((valInKey = bis.readLine()) != null) {
												sb.append(valInKey);
											}

										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										}
										String valuetobereturned = sb.toString();

										//	mt = new MatrixCursor(column);
										String row[] = new String[]{selection, valuetobereturned};
										mt.addRow(row);
										//dny
										noneedofQF3 = 1;
									}
								} else {
									//form socket connection with succ1
									Log.e("SockConnFlag==1", "RemoteQuery");
									Socket sockets1 = null;
									try {
										sockets1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												Integer.parseInt(REMOTE_PORT4));
										DataOutputStream dos1 = new DataOutputStream(sockets1.getOutputStream());
										Log.e("*Q:Msg sent to SUCC 1 is:", cKeyVal);
										//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
										dos1.writeUTF(cKeyVal);
									} catch (SocketTimeoutException e) {
										Log.e("*Query:failAvd3->Succ1", "SockeTimeout");
										failAvd3 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("*Query:failAvd3->Succ1", "EOFException");
										failAvd3 = 2;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("*Query:failAvd3->Succ1", "SocketException");
										failAvd3 = 2;

										e.printStackTrace();
										// Node Failure

									} catch (IOException e) {
										Log.e("*Query:failAvd3->Succ1", "IOException");
										failAvd3 = 2;
										e.printStackTrace();
									} catch (Exception e) {
										Log.e("*Query:failAvd3->Succ1", "Exception");
										failAvd3 = 2;
										e.printStackTrace();
									}
								}
							}
						}//end of failAvd3

						if (noneedofQF3 != 1)
						{
							Log.e("*Q: Before *While* QueryFlagStar:", String.valueOf(QueryFlagStar));

							while (QueryFlagStar == 0) {

							}
							Log.e("*Q: After *WHile* QueryFlagStar:", String.valueOf(QueryFlagStar));
							Log.e("STAR Query:After WHile starShared:-------->", String.valueOf(starShared));

							if (starShared.size() != 0) {
								Log.e("Myport ----------->:", myPortInsert);
								Log.e("Final StarShared:size:", String.valueOf(starShared.size()));

								for (int d = 0; d < starShared.size(); d++) {

									String UB = String.valueOf(starShared.get(d));
									if (UB != null) {
										String[] UBcse = UB.split("#");
										if (UBcse.length > 1) {
											//d						Log.e("UB Key-value from Final starShared:", UB);
											String selection1 = UBcse[0];
											//d						Log.e("UB Key ------------------>:", UBcse[0]);
											String valuetobereturned = UBcse[1];
											//d					Log.e("UB valuetobereturned ------------------>:", UBcse[1]);

											String row[] = new String[]{selection1, valuetobereturned};
											mt.addRow(row);
										}
									}
								}
							}
							Log.e("mt size", String.valueOf(starShared.size()));
							QueryFlagStar = 0;
							//if(QueryFlag!=0)
							//{
							//    Log.e("Query: QueryAns", queryAns);
							//"KeyQuery key word ahe ignore kar" + "#" + KeyQuery+"#"+myPortInsert;
							//queryAns=lastArr[2]+"#"+lastArr[2];//selection+#+valu

							String[] tempQans = queryAns.split("#");
							String selection1 = tempQans[0];
							String valuetobereturned = tempQans[1];
							Log.e("End of Qurying 5560:QuryFlagStar", String.valueOf(QueryFlagStar));
							// Log.e("Query: ans)", String.valueOf(queryAns));
							// mt = new MatrixCursor(column);

							//}
							//   Log.e("Query: After  if(QueryFlag!=0)", String.valueOf(QueryFlag));
							starShared.clear();

						}
					}

				}

			}
		}
		else if (selection.equals("@"))
		{
			newflag=1;
			Log.e("Entered in @", selection);
			Log.e("in @:", results.toString());

			String[] files = ctx1.fileList();
			if (files.length > 0) {
				for (String file : files) {
					{
						results.add(file);

						Log.e("KEYname of file:", file);
					}
				}
			}
			//d		Log.e("list of files:results:", results.toString());
			//d		Log.e("in @ size of results:", String.valueOf(results.size()));

			File f = new File(getContext().getFilesDir().getAbsolutePath());
			for (int c = 0; c < results.size(); c++)
			{
				StringBuffer sb = new StringBuffer();
				FileInputStream fis = null;
				try
				{
					fis = ctx1.getApplicationContext().openFileInput(results.get(c));
					BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
					while ((valInKey = bis.readLine()) != null)
					{
						sb.append(valInKey);
						//d			Log.e("in @ print c", String.valueOf(c));
						//d			Log.e("File f:", f.toString());
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				String valuetobereturned = sb.toString();
				//d		Log.e("append(value2breturnd):", valuetobereturned);

				String row[] = new String[]{results.get(c), valuetobereturned};
				mt.addRow(row);
				//d			Log.e("mt.row: @@@:", String.valueOf(row));
				//d			Log.e("int C @@@:", results.get(c) + "*" + c);
				//d			Log.e("results.toString @@@", results.toString());
				//d		Log.e("mt ****", mt.toString());
				//  return mt;
			}

		}
		else {
			//synchronized (this)
			{
				Log.e("Cursor query not * or @:", selection);
				int sockConnflag = 0;
				socketreply=0;
				String ins = selection;

				{
					String hashedKey = null;
					try {
						hashedKey = genHash(ins);
						Log.e("QUERY:Hashed key", hashedKey);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}


					al.add(hashedKey);
					//after adding key sort Al
					Collections.sort(al, new HashComp());
					//d			Log.e("QUERY:after add+Sortng", String.valueOf(al));

					//main for loop to access Al with Key in it
					for (int i = 0; i < al.size(); i++) {
						//determine if it is a Key or a Node
						String temp = (String) al.get(i);
						//Log.e("QUERY: String from Al:", temp);
						String[] tempArr = temp.split("#");
						if (tempArr.length == 2)//this is a node//do nothing
						{
							//Log.e("QUERY:This is a NODE with port from Al", "");

						} else {
							// this is a key//insert it to the next port number from AL//tempArr=Key
							int j, k;
							{
								Log.e("QUERY:index of key in AL:", String.valueOf(i));
								int flag = 0;
								if (i == al.size() - 1)//c1//key in last idex
								{
									//Log.e("Thorve c1 i:", String.valueOf(i));
									i = 0;
									j = i + 1;
									k = i + 2;
									flag = 1;
									//Log.e("QUERY:Thorve c1 flag:", String.valueOf(flag));
									//Log.e("QUERY:Thorve c1 i:", String.valueOf(i));
									//Log.e("QUERY:Thorve c1 j:", String.valueOf(j));
									//Log.e("QUERY:Thorve c1 k:", String.valueOf(k));

								} else {
									i = i + 1;
									if (i == al.size() - 1)//target at last index
									{
										j = 0;
										k = j + 1;
										//	Log.e("QUERY:Key in 2nd last index i:", String.valueOf(j));
										//	Log.e("QUERY:Key in 2nd last index j:", String.valueOf(j));
										//	Log.e("QUERY:key in 2nd last index k:", String.valueOf(k));

									} else if (i == al.size() - 2)//target 2nd last
									{
										j = al.size() - 1;
										k = 0;
										//	Log.e("QUERY:Key in 3rd last index i:", String.valueOf(j));
										//	Log.e("QUERY:Key in 3rd last index j:", String.valueOf(j));
										//	Log.e("QUERY:key in 3rd last index k:", String.valueOf(k));
									} else {
										j = i + 1;
										k = j + 1;
										//	Log.e("QUERY:index i:", String.valueOf(j));
										//	Log.e("QUERY:index j:", String.valueOf(j));
										//	Log.e("QUERY:index k:", String.valueOf(k));
									}
									//	Log.e("QUERY:dny c1 i:", String.valueOf(i));
									flag = 2;
									//	Log.e("QUERY:dny c1 flag:", String.valueOf(flag));
								}

								//	Log.e("QUERY:Outside Avd index i:", String.valueOf(i));
								//	Log.e("QUERY:Outside index j:", String.valueOf(j));
								//	Log.e("QUERY:Outside index k:", String.valueOf(k));

								String targetAVD = (String) al.get(i);//target avd
								String targetAVDsucc1 = (String) al.get(j);//target avd succ1
								String targetAVDsucc2 = (String) al.get(k);//target avd succ2

								//	Log.e("QUERY:targetAVD= al.get(i):", String.valueOf(i));
								//	Log.e("QUERY:targetAVDsucc=al.get(j):", String.valueOf(j));
								//	Log.e("QUERY:targetAVDsucc2=:al.get(k)", String.valueOf(k));

								//for target avd handle conditions


								if (flag == 1) {
									i = al.size() - 1;//restore i
									//	Log.e("QUERY:Thorve 2 c1 i:", String.valueOf(i));
									//	Log.e("QUERY:Thorve c1 flag:", String.valueOf(flag));
								}
								if (flag == 2) {
									i = i - 1;
									//	Log.e("QUERY:dny 2 c1 i:", String.valueOf(i));
									//	Log.e("QUERY:dny 2 c1 flag:", String.valueOf(flag));
								}

								//it should give you the hashed string of a port number which is next to you in AL
								//d			Log.e("QUERY:String target avd--> port no:", targetAVD);
								//d			Log.e("QUERY:targetAVDsucc1------> port no:", targetAVDsucc1);
								//d			Log.e("QUERY:targetAVDsucc2---------> port no:", targetAVDsucc2);

								//you get hashed node id and port no in tempArr2[1] where the file will be stored
								//port no:tempArr2[1]
								//Target port now known
								//d		Log.e("QUERY:i value is:", String.valueOf(i));
								String[] tempArr2sukanya = targetAVD.split("#");
								String[] tempArr2Succ1 = targetAVDsucc1.split("#");
								String[] tempArr2Succ2 = targetAVDsucc2.split("#");
								int p = Integer.parseInt(tempArr2sukanya[1]) * 2;
								int succ1 = Integer.parseInt(tempArr2Succ1[1]) * 2;
								int succ2 = Integer.parseInt(tempArr2Succ2[1]) * 2;

								//d Log.e("tempArr2sukanya[1].toString():", tempArr2sukanya[1].toString());
								//	Log.e("targetAVDsucc1[].tostring:", targetAVDsucc1.toString());
								//d	Log.e("succ1 :", String.valueOf(succ1));
								//Log.e("targetAVDsucc2[]..tostring :", targetAVDsucc2.toString());
								//d	Log.e("succ2 :", String.valueOf(succ2));

								al.remove(i);//remove key from al

								Log.e("al.size() after removing key :", String.valueOf(al.size()));

								//	for (int d = 0; d < al.size(); d++)
								{
									//	Log.e("After Removing key Al element:" + d + ":", String.valueOf(al.get(d)));
								}

								Log.e("gradwalk@ Test 3 After", " Key Removed");

								Collections.sort(al, new HashComp());

								for (int d = 0; d < al.size(); d++) {
									//d		Log.e("Final AL element :" + d + ":", String.valueOf(al.get(d)));
								}

								TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
								String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
								final String myPortInsert = String.valueOf((Integer.parseInt(portStr) * 2));
								int mePort = Integer.parseInt(myPortInsert) / 2;
								//Debug pt // I am the target port, store the file
								Log.e("QUERY mePort is:", String.valueOf(mePort));


								if (mePort == Integer.parseInt(tempArr2sukanya[1])) {
									///locally fetch

									Log.e("Find key inside me:Target==meport:", String.valueOf(mePort));

									File f = new File(getContext().getFilesDir().getAbsolutePath());
									FileInputStream fis = null;
									StringBuffer sb = new StringBuffer();
									try {
										fis = ctx1.getApplicationContext().openFileInput(selection);

										Log.e("Query File f:", f.toString());
										BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
										while ((valInKey = bis.readLine()) != null) {

											sb.append(valInKey);
										}

									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									String valuetobereturned = sb.toString();

									//	mt = new MatrixCursor(column);
									String row[] = new String[]{selection, valuetobereturned};
									mt.addRow(row);
									noneedQF=1;
									//Log.e("Query :mt.row:", String.valueOf(row));
									Log.e("Query :mt normal", mt.toString());

									Log.e("Query :append(value2breturnd):", valuetobereturned);

									///

								} else if (!tempArr2sukanya[1].equals(String.valueOf(mePort)))//socket connection with target and n
								{

									Log.e("Q:Goto Target port:" + tempArr2sukanya[1],"yo");
									Log.e("from myPortInsert:" + myPortInsert, "yo");
									//	Log.e("Query: Before Query Flag:", String.valueOf(QueryFlag));
									String KeyQuery = selection;


									String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + mePort;


									Socket socketQ = new Socket();
									DataInputStream din;
									try {
										//try {
										//	Thread.sleep(2000);
										//} catch (InterruptedException e) {
										//	e.printStackTrace();
										//}
										Log.e("Sending msg to Port p:", String.valueOf(p));
										socketQ = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),p);
										socketreply=0;
										//SocketAddress sockAddress=new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),p);//last change
										Log.e("a", "aaaaa");
										socketQ.setSoTimeout(2000);
										//	socketQ.connect(sockAddress, 2000);
										Log.e("b", "bbbbbbb");

										DataOutputStream  dos = new DataOutputStream(socketQ.getOutputStream());
										din=new DataInputStream(socketQ.getInputStream());
										Log.e("c", "cccccccc");
										String aru=din.readUTF();
										Log.e("aru", aru);
										if(!aru.isEmpty() || aru!=null || !aru.equals(""))
										{
											Log.e("rcvd str ->", aru);
											Log.e("Msg to target avd is:", cKeyVal);
											dos.writeUTF(cKeyVal);
										}
										else
										{
											sockConnflag = 1;
											Log.e("aru:sockConnflag = 1.",String.valueOf(sockConnflag));
										}
										//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;


										//	dos.close();

									} catch (SocketTimeoutException e) {
										Log.e("Q:SOCKET CONN TARGET AVD FAILED", "SockeTimeout SUCC 1");
										Log.e("Q1:SOCKETFAILED", "SoTimut SUCC 1");
										sockConnflag = 1;
										e.printStackTrace();
										// Node Failure

									} catch (EOFException e) {
										Log.e("Query:SOCKET CONNECTION WITH TARGET AVD HAS FAILED", "EOFException SUCC 1");
										Log.e("Q2:SOCKETFAILED", "SoTimut SUCC 1");
										sockConnflag = 1;
										e.printStackTrace();
										// Node Failure

									} catch (SocketException e) {
										Log.e("Query:SOCKET CONNECTION WITH TARGET AVD HAS FAILED", "SocketException SUCC 1");
										sockConnflag = 1;
										Log.e("Q3:SOCKETFAILED", "SoTimut SUCC 1");
										e.printStackTrace();
										// Node Failure

									} catch (IOException e) {
										Log.e("Query: SOCKET CONNECTION WITH TARGET AVD HAS FAILED", "IOException SUCC 1");
										Log.e("Q4:SOCKETFAILED", "SoTimut SUCC 1");
										sockConnflag = 1;
										e.printStackTrace();
									} catch (Exception e) {
										Log.e("Query: SOCKET CONNECTION WITH TARGET AVD HAS FAILED", "Exception SUCC 1");
										Log.e("Q5:SOCKETFAILED", "SoTimut SUCC 1");
										sockConnflag = 1;
										e.printStackTrace();
									}
									finally {
										try {
											//socketQ.close(); //close resources here!
											socketQ.close();

										} catch (IOException e) {
											e.printStackTrace();
										}

									}
									/*
									Log.e("ScoketReply(0)", String.valueOf(socketreply));
									Log.e("Thread sleeping", "now");
									try {
										Thread.sleep(2500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									Log.e("Thread awake", "now");
									Log.e("ScoketReply(1)", String.valueOf(socketreply));
									/* aru changes
									if(socketreply==0)
									{
										sockConnflag=1;
										Log.e("Failure detected", "now");
									}
									Log.e("ScoketReply(1)", String.valueOf(socketreply));
									*/
									//	if (sockConnflag == 1 || socketreply==0) {
									if (sockConnflag == 1 ) {
										if (mePort == (succ1 / 2)) {
											{
												///locally fetch

												Log.e("Find key inside me:SUCC1==meport:", String.valueOf(mePort));

												File f = new File(getContext().getFilesDir().getAbsolutePath());
												FileInputStream fis = null;
												StringBuffer sb = new StringBuffer();
												try {
													fis = ctx1.getApplicationContext().openFileInput(selection);

													Log.e("Query File f:", f.toString());
													BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
													while ((valInKey = bis.readLine()) != null) {

														sb.append(valInKey);
													}

												} catch (FileNotFoundException e) {
													e.printStackTrace();
												} catch (IOException e) {
													e.printStackTrace();
												}
												String valuetobereturned = sb.toString();

												//	mt = new MatrixCursor(column);
												String row[] = new String[]{selection, valuetobereturned};
												mt.addRow(row);
												//dny
												noneedQF=1;
												Log.e("Query :mt.row:", String.valueOf(row));
												Log.e("Query :mt normal", mt.toString());

												Log.e("Query :append(value2breturnd):", valuetobereturned);
												///

											}
										} else {
											//form socket connection with succ1
											Log.e("SockConnFlag==1", "RemoteQuery");
											//Socket sockets1 = null;
											try {
												Socket sockets1 = new Socket();
												sockets1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
														succ1);
												sockets1.setSoTimeout(2000);
												DataOutputStream dos1 = new DataOutputStream(sockets1.getOutputStream());
												Log.e("Msg sent to SUCC 1 is:", cKeyVal);
												//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
												dos1.writeUTF(cKeyVal);
											}catch (SocketTimeoutException e) {
												Log.e("Query:SOCKET CONNECTION WITH SUCC1 HAS FAILED", "SockeTimeout");
												sockConnflag = 2;
												e.printStackTrace();
												// Node Failure

											} catch (EOFException e) {
												Log.e("Query:SOCKET CONNECTION WITH succ1 AVD HAS FAILED", "EOFException");
												sockConnflag = 2;
												e.printStackTrace();
												// Node Failure

											} catch (SocketException e) {
												Log.e("Query:SOCKET CONNECTION WITH Succ1 HAS FAILED", "SocketException");
												sockConnflag = 2;

												e.printStackTrace();
												// Node Failure

											} catch (IOException e) {
												Log.e("Query: SOCKET CONNECTION WITH succ1 HAS FAILED", "IOException");
												sockConnflag = 2;
												e.printStackTrace();
											} catch (Exception e) {
												Log.e("Query: SOCKET CONNECTION WITH succ1 HAS FAILED", "Exception");
												sockConnflag = 2;
												e.printStackTrace();
											}

										}
									}
									if (sockConnflag == 2) {

										if (mePort == (succ2 / 2)) {
											{
												///locally fetch

												Log.e("Find your key inside me:SUCC2==meport:", String.valueOf(mePort));

												File f = new File(getContext().getFilesDir().getAbsolutePath());
												FileInputStream fis = null;
												StringBuffer sb = new StringBuffer();
												try {
													fis = ctx1.getApplicationContext().openFileInput(selection);

													Log.e("Query File f:", f.toString());
													BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
													while ((valInKey = bis.readLine()) != null) {

														sb.append(valInKey);
													}

												} catch (FileNotFoundException e) {
													e.printStackTrace();
												} catch (IOException e) {
													e.printStackTrace();
												}
												String valuetobereturned = sb.toString();

												//	mt = new MatrixCursor(column);
												String row[] = new String[]{selection, valuetobereturned};
												mt.addRow(row);
												noneedQF=1;
												Log.e("Query :mt.row:", String.valueOf(row));
												Log.e("Query :mt normal", mt.toString());

												Log.e("Query :append(value2breturnd):", valuetobereturned);
												///

											}
										} else {
											Socket sockets2 = new Socket();
											try {
												sockets2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
														succ2);
												DataOutputStream dos2 = new DataOutputStream(sockets2.getOutputStream());
												Log.e("Msg to SUCC 2 is:"+succ2, cKeyVal);
												//String cKeyVal = "KeyQuery" + "#" + KeyQuery + "#" + myPortInsert;
												dos2.writeUTF(cKeyVal);
											}


											catch (SocketTimeoutException e) {
												Log.e("Query:SOCKET CONNECTION WITH succ2 HAS FAILED", "SockeTimeout");
												//	sockConnflag = 1;
												e.printStackTrace();
												// Node Failure

											} catch (EOFException e) {
												Log.e("Query:SOCKET CONNECTION WITH succ2 HAS FAILED", "EOFException");
												//sockConnflag = 1;
												e.printStackTrace();
												// Node Failure

											} catch (SocketException e) {
												Log.e("Query:SOCKET CONNECTION WITH succ2 HAS FAILED", "SocketException");
												//sockConnflag = 1;

												e.printStackTrace();
												// Node Failure

											} catch (IOException e) {
												Log.e("Query: SOCKET CONNECTION WITH succ2 HAS FAILED", "IOException");
												//sockConnflag = 1;
												e.printStackTrace();
											} catch (Exception e) {
												Log.e("Query: SOCKET CONNECTION WITH succ2 HAS FAILED", "Exception ");
												//sockConnflag = 1;
												e.printStackTrace();
											}
										}
									}
									//QueryFlag=0;//sukku
									//	Log.e("Query sent succesfully to target", temp);
									synchronized (this) {
										if (noneedQF != 1 )
										{
											Log.e("Q: Before *WHILE* QueryFlag:", String.valueOf(QueryFlag));
											newflag = 1;
											while (QueryFlag == 0) {

											}
											Log.e("Q:After *WHILE* QueryFlag:", String.valueOf(QueryFlag));
											QueryFlag = 0;
											//if(QueryFlag!=0)
											//{
											//    Log.e("Query: QueryAns", queryAns);
											//"KeyQuery key word ahe ignore kar" + "#" + KeyQuery+"#"+myPortInsert;
											//queryAns=lastArr[2]+"#"+lastArr[2];//selection+#+valu
											String[] tempQans = queryAns.split("#");
											String selection1 = tempQans[0];
											String valuetobereturned = tempQans[1];
											Log.e("end of Query:QueryFlag=", String.valueOf(QueryFlag));
											//Log.e("Query: ans)", String.valueOf(queryAns));
											// mt = new MatrixCursor(column);
											String row[] = new String[]{selection1, valuetobereturned};
											mt.addRow(row);
											//}
											Log.e("Query ends.so value of QueryFlag", String.valueOf(QueryFlag));
											Log.e("SYNC ends", "Hurray");
											//break;
											//return  mt;
										}
									}

								}
							}

						}
					}


				}

			}
		}
		Log.e("mt size", String.valueOf(mt.getCount()));

		return mt;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}


	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		int myPortInt= Integer.parseInt(myPort)/2;

		protected   Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			try {
				while(true) {

					Socket s = serverSocket.accept();

//					InputStream inStream = s.getInputStream();
//					ObjectInputStream oInStream = new ObjectInputStream(inStream);


					DataInputStream dis = new DataInputStream(s.getInputStream());
					DataOutputStream os  = new DataOutputStream(s.getOutputStream());
					os.writeUTF("Connected");
					String str1 = dis.readUTF();
				//	String str1 = null;


					Log.e("------------------------->  ST doInBack", str1);
					//str1="JoinPlease"+"#"+myPort;
					//NoStandAlone![177ccecaec32c54b82d5aaafc18a2dadb753e3b1#11124,
					// 208f7f72b198dadd244e61801abe1ec3a4857bc9#11112, 33d6357cfaaf0f72991b0ecd8c56da066613c089#11108,
					// abf0fd8db03e5ecb199a9b82929e9db79b909643#11116, c25ddd596aa7c81fa12378fa725f706d54325d12#11120]
					//cKeyVal = "KeyQuery" + "#" + KeyQuery;
					//cKeyVal = "StarKeyQuery" + "#" + selection+"#"+myPortInsert;
					//final String msgPred="PredRejoin"+"#"+myPort;
          /* Get Hash of Ports */
					{
						String[] strArr = str1.split("#");
						String s1 = strArr[0];
						String s2 = strArr[1];

						if (s1.equals("Rejoin") ) {
							synchronized (this)
							{

								if (newflag == 1) {
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}


								//final String msg="Rejoin"+"#"+myPort;
								repResults.clear();
								Log.e("ST $ DNY REPLication Works", "conn works");
								//Log.e("Rejoin Request from ", s2);
								Log.e("Coming from port number:", s2);
								Log.e("repResults size:(0)", String.valueOf(repResults.size()));
								File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
								if (files != null) {
									for (int k = 0; k < files.length; k++) {
										if (files[k].isFile()) {
											Log.e("KEYname of file:", files[k].getName());
											String sname = files[k].getName();

											String hashedKey = null;
											try {
												hashedKey = genHash(sname);
												//Log.e("REJOIN:Hashed key Name", hashedKey);
											} catch (NoSuchAlgorithmException e) {
												e.printStackTrace();
											}

											al.add(hashedKey);
											int x, y;
											Collections.sort(al, new HashComp());
											for (int i = 0; i < al.size(); i++) {

												//determine if it is a Key or a Node
												String temp = (String) al.get(i);
												//Log.e("Typecast: String from Al:", temp);
												String[] tempArr = temp.split("#");

												if (tempArr.length == 2)//this is a node//do nothing
												{
													//		Log.e("This is a NODE with port from Al", "");
												} else// this is a key//insert it to the next port number from AL//tempArr=Key
												{
													//		Log.e("index of key in AL:", String.valueOf(i));
													int flag = 0;
													if (i == al.size() - 1)//c1//key in last idex
													{
														//		Log.e("Thorve c1 i:", String.valueOf(i));
														i = 0;
														x = i + 1;
														y = i + 2;
														flag = 1;
													} else {
														i = i + 1;
														if (i == al.size() - 1)//target at last index
														{
															x = 0;
															y = x + 1;
														} else if (i == al.size() - 2)//target 2nd last
														{
															x = al.size() - 1;
															y = 0;
														} else {
															x = i + 1;
															y = x + 1;
														}
														flag = 2;
													}

													String targetAVD = (String) al.get(i);//target avd
													String targetAVDsucc1 = (String) al.get(x);//target avd succ1
													String targetAVDsucc2 = (String) al.get(y);//target avd succ2

													//for target avd handle conditions

													if (flag == 1) {
														i = al.size() - 1;//restore i
													}
													if (flag == 2) {
														i = i - 1;
													}
													//it should give you the hashed string of a port number which is next to you in AL
													Log.e("String target avd--> port no:", targetAVD);
													Log.e("targetAVDsucc1------> port no:", targetAVDsucc1);
													Log.e("targetAVDsucc2---------> port no:", targetAVDsucc2);

													//you get hashed node id and port no in tempArr2[1] where the file will be stored
													//port no:tempArr2[1]
													//Target port now known
													//Log.e("i value is:", String.valueOf(i));

													String[] tempArr2sukanya = targetAVD.split("#");
													String[] tempArr2Succ1 = targetAVDsucc1.split("#");
													String[] tempArr2Succ2 = targetAVDsucc2.split("#");

													int tport = Integer.parseInt(tempArr2sukanya[1]) * 2;
													int succ1 = Integer.parseInt(tempArr2Succ1[1]) * 2;
													int succ2 = Integer.parseInt(tempArr2Succ2[1]) * 2;

													if (myPort.equals(String.valueOf(tport))) {
														//Log.e("Do Nothing.Key belongs to this Avd", files[k].getName());//replica logic
													} else if (s2.equals(String.valueOf(tport))) {
														Log.e("observe this:", files[k].getName());
														FileInputStream fis = null;
														StringBuffer sb = new StringBuffer();
														Context ctx1 = getContext();
														String valInKey = "";
														try {
															File f = new File(getContext().getFilesDir().getAbsolutePath());
															fis = ctx1.getApplicationContext().openFileInput(files[k].getName());
															Log.v("File f:", f.toString());
															BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
															while ((valInKey = bis.readLine()) != null) {

																sb.append(valInKey);
															}

														} catch (FileNotFoundException e) {
															e.printStackTrace();
														} catch (IOException e) {
															e.printStackTrace();
														}
														String valuetobereturned = sb.toString();
														//Log.e("valuetobereturned", valuetobereturned);
														String keyvalpair = files[k].getName() + "#" + valuetobereturned;
														//	Log.e("keyvalpair", keyvalpair);
														repResults.add(keyvalpair);
													}
													al.remove(i);//remove key from al
												}
											}//for

										}
									}//file for
								}
								Log.e("repResult @replica-->", String.valueOf(repResults));
								Log.e("predResults size:", String.valueOf(repResults.size()));

								String sam = "RejoinReply" + "#" + repResults.toString();

								Socket socketR = null;
								try {
									socketR = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											Integer.parseInt(s2));
									DataOutputStream dos = new DataOutputStream(socketR.getOutputStream());
									Log.e("RepResults sent2newly joind node", sam);
									dos.writeUTF(sam);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}//sync

						}
						else if(s1.equals("RejoinReply"))
						{
							//Log.e("Recieved String repResults str1 :", s2);

							//Log.e("reply str", String.valueOf(str1.length()));

							//String predSam="PredRejoinReply"+"#"+predResults.toString();

							//Log.e("str1 rcvd in ST->", str1);
							//String[] davis = str1.split("\\$");

							String newstr=str1.replace("RejoinReply#","");
							//Log.e("target PredList->", davis[1]);

							//String lastVal = davis[1];jok
							String lastVal = newstr;
							//Log.e("Focus here----->newstr->raw repResults->:", lastVal);

							String replace = lastVal.replace("[", "");
							//Log.e("replace:--------->", replace);
							String replace1 = replace.replace("]", "");
							//	Log.e("replace1:--------->", replace1);

							List<String> myList = new ArrayList<String>(Arrays.asList(replace1.split(",")));
							//	Log.e("String convert", myList.toString());

							for (int k = 0; k < myList.size(); k++)
							{
								repResults.add(myList.get(k).trim());
							}

							//	Log.e("Final REP:Results in newly joined node:", myPort + " " +repResults.toString());
							Log.e("Final REP results:", myPort + " " + String.valueOf(repResults.size()));

							replicaFetch=1;
						}
						else if(s1.equals("PredRejoin")) {


							synchronized (this)
							{

								if (newflag == 1) {
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}


								predResults.clear();
								Log.e("ST $ Predessor backup Works", "conn works");
								//Log.e("Rejoin Request from ", s2);
								Log.e("Coming from port number:", s2);
								Log.e("predResults size:(0)", String.valueOf(predResults.size()));
								File[] files = new File("/data/data/edu.buffalo.cse.cse486586.simpledynamo/files").listFiles();
								if (files != null) {
									for (int k = 0; k < files.length; k++) {
										if (files[k].isFile()) {
											//Log.e("PRED:KEYname of file:", files[k].getName());
											String sname = files[k].getName();

											String hashedKey = null;
											try {
												hashedKey = genHash(sname);
												//Log.e("REJOIN:Hashed key Name", hashedKey);
											} catch (NoSuchAlgorithmException e) {
												e.printStackTrace();
											}

											al.add(hashedKey);
											int x, y, z;//z is index for predpred
											Collections.sort(al, new HashComp());
											for (int i = 0; i < al.size(); i++) {

												//determine if it is a Key or a Node
												String temp = (String) al.get(i);
												//Log.e("Typecast: String from Al:", temp);
												String[] tempArr = temp.split("#");

												if (tempArr.length == 2)//this is a node//do nothing
												{
													//	Log.e("This is a NODE with port from Al", "");
												} else// this is a key//insert it to the next port number from AL//tempArr=Key
												{
													//	Log.e("index of key in AL:", String.valueOf(i));
													int flag = 0;
													if (i == al.size() - 1)//c1//key in last idex
													{
														//		Log.e("Thorve c1 i:", String.valueOf(i));
														i = 0;
														x = i + 1;
														y = i + 2;
														flag = 1;
													} else {
														i = i + 1;
														if (i == al.size() - 1)//target at last index
														{
															x = 0;
															y = x + 1;
														} else if (i == al.size() - 2)//target 2nd last
														{
															x = al.size() - 1;
															y = 0;
														} else {
															x = i + 1;
															y = x + 1;
														}
														flag = 2;
													}

													String targetAVD = (String) al.get(i);//target avd
													String targetAVDsucc1 = (String) al.get(x);//target avd succ1
													String targetAVDsucc2 = (String) al.get(y);//target avd succ2

													//for target avd handle conditions

													if (flag == 1) {
														i = al.size() - 1;//restore i
													}
													if (flag == 2) {
														i = i - 1;
													}
													//it should give you the hashed string of a port number which is next to you in AL
													Log.e("String target avd--> port no:", targetAVD);
													Log.e("targetAVDsucc1------> port no:", targetAVDsucc1);
													Log.e("targetAVDsucc2---------> port no:", targetAVDsucc2);

													//you get hashed node id and port no in tempArr2[1] where the file will be stored
													//port no:tempArr2[1]
													//Target port now known
													//Log.e("i value is:", String.valueOf(i));

													String[] tempArr2sukanya = targetAVD.split("#");
													String[] tempArr2Succ1 = targetAVDsucc1.split("#");
													String[] tempArr2Succ2 = targetAVDsucc2.split("#");

													int tport = Integer.parseInt(tempArr2sukanya[1]) * 2;
													int succ1 = Integer.parseInt(tempArr2Succ1[1]) * 2;
													int succ2 = Integer.parseInt(tempArr2Succ2[1]) * 2;
													int prepred = 0;


													if (myPort.equals(String.valueOf(tport))) {

														Log.e("PRED:observe this Predessor*:", files[k].getName());
														FileInputStream fis = null;
														StringBuffer sb = new StringBuffer();
														Context ctx1 = getContext();
														String valInKey = "";
														try {
															File f = new File(getContext().getFilesDir().getAbsolutePath());
															fis = ctx1.getApplicationContext().openFileInput(files[k].getName());
															//	Log.v("Pred* File f:", f.toString());
															BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
															while ((valInKey = bis.readLine()) != null) {
																sb.append(valInKey);
															}

														} catch (FileNotFoundException e) {
															e.printStackTrace();
														} catch (IOException e) {
															e.printStackTrace();
														}
														String valuetobereturned = sb.toString();
														//Log.e("Pred* valuetobereturned", valuetobereturned);
														String keyvalpair = files[k].getName() + "#" + valuetobereturned;
														//	Log.e("Pred* keyvalpair", keyvalpair);


														predResults.add(keyvalpair);

														Log.e("PRED 123456 :size of predResults*:", String.valueOf(predResults.size()));
													}

													if (myPort.equals(String.valueOf("11124"))) {
														prepred = 11120;
														Log.e("PRED***PRED:", String.valueOf(prepred));
													}
													if (myPort.equals(String.valueOf("11112"))) {
														prepred = 11124;
													}
													if (myPort.equals(String.valueOf("11108"))) {
														prepred = 11112;
														Log.e("PRED***PRED:", String.valueOf(prepred));
													}
													if (myPort.equals(String.valueOf("11116"))) {
														prepred = 11108;
														Log.e("PRED***PRED:", String.valueOf(prepred));
													}
													if (myPort.equals(String.valueOf("11120"))) {
														prepred = 11116;
														Log.e("PRED***PRED:", String.valueOf(prepred));
													}
													if (tport == prepred) {

														Log.e("PRED***PRED:observe Predessor*:", files[k].getName());
														Log.e("PRED***PRED:size of predResults*:", String.valueOf(predResults.size()));
														FileInputStream fis = null;
														StringBuffer sb = new StringBuffer();
														Context ctx1 = getContext();
														String valInKey = "";
														try {
															File f = new File(getContext().getFilesDir().getAbsolutePath());
															fis = ctx1.getApplicationContext().openFileInput(files[k].getName());
															//	Log.v("Pred* File f:", f.toString());
															BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
															while ((valInKey = bis.readLine()) != null) {
																sb.append(valInKey);
															}

														} catch (FileNotFoundException e) {
															e.printStackTrace();
														} catch (IOException e) {
															e.printStackTrace();
														}
														String valuetobereturned = sb.toString();
														//Log.e("Pred* valuetobereturned", valuetobereturned);
														String keyvalpair = files[k].getName() + "#" + valuetobereturned;
														//	Log.e("Pred* keyvalpair", keyvalpair);


														predResults.add(keyvalpair);

													} else if (s2.equals(String.valueOf(tport))) {
														//	Log.e("this data need not go to requesting avd", files[k].getName());
													}
													al.remove(i);//remove key from al---->remember
												}
											}//for

										}
									}//file for
								}

								Log.e("PredResult from predessor:--->", predResults.toString());
								Log.e("predResults size:", String.valueOf(predResults.size()));
								String predSam = "PredRejoinReply" + "#" + predResults.toString();
								Socket socketR1 = null;
								try {
									socketR1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											Integer.parseInt(s2));
									DataOutputStream dos11 = new DataOutputStream(socketR1.getOutputStream());
									Log.e("PredResult sent2newly joind node", predSam);
									dos11.writeUTF(predSam);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}//sync

						}
						else if(s1.equals("PredRejoinReply"))
						{

							//	Log.e("Recieved String repResults str1 :", str1);

							//Log.e("reply str", String.valueOf(str1.length()));

							//String predSam="PredRejoinReply"+"#"+predResults.toString();

							//Log.e("str1 rcvd in ST->", str1);
							//String[] davis = str1.split("\\$");

							String newstr=str1.replace("PredRejoinReply#","");
							//Log.e("target PredList->", davis[1]);

							//String lastVal = davis[1];jok
							String lastVal = newstr;
							//Log.e("Focus here----->newstr->raw *PRED*Results->:", lastVal);

							String replace = lastVal.replace("[", "");
							//Log.e("replace:--------->", replace);
							String replace1 = replace.replace("]", "");
							//	Log.e("replace1:--------->", replace1);

							List<String> myList = new ArrayList<String>(Arrays.asList(replace1.split(",")));
							//Log.e("String convert", myList.toString());


							for (int k = 0; k < myList.size(); k++)
							{
								predResults.add(myList.get(k).trim());
							}

							//Log.e("Final PredResults in newly joined node:", myPort + " " + predResults.toString());
							Log.e("Final Pred results:", myPort + " " + String.valueOf(predResults.size()));

							predFetch=1;

						}


						//final String msgPred="PredRejoin"+"#"+myPort;
						else if (s1.equals("KeyAdd"))
						{

							synchronized (this)
							{
								if(newflag==1)
								{
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								Log.e("else if entered in ST: KeyAdd :", String.valueOf(str1));


								Log.e("ST : myport :", myPort);
								//KeyAdd#ilfe3rdhEVMpde7KwbswJKjBRyCYtEQy#MA7i98iuesUI2optqJaQNJKuNHyRU7Pg
								//KeyAdd#key#value

								String[] tempKeyAdd = str1.split("#");
								String cKey = tempKeyAdd[1];
								String cVal = tempKeyAdd[2];
								//String keyValueToInsert=cKey+cVal;

								ContentValues keyValueToInsert = new ContentValues();
								keyValueToInsert.put("key", cKey);
								keyValueToInsert.put("value", cVal);
								// Uri newUri = getContext().getContentResolver().insert(uri, keyValueToInsert);
								String ins = "" + keyValueToInsert.get("key");


								Log.e("ST TArget AVD insrtion:", keyValueToInsert.toString());

								Context ctx = getContext();
								String filename = "" + ins;

								String string = keyValueToInsert.get("value") + "\n";
								FileOutputStream outputStream;
								try {
									outputStream = ctx.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
									outputStream.write(string.getBytes());
									outputStream.close();
								} catch (Exception e) {
									Log.e("Except caught is:", e.toString());
								}
								Log.e("target avd inserting CV:", myPort + "----" + keyValueToInsert.toString());

								Log.e("Added values in target avd by content resolver:", String.valueOf(keyValueToInsert));
								// Log.e("newUri:", String.valueOf(newUri));

							}
						}

						else if(s1.equals("KeyQuery")){
							//synchronized(this)//last change
							{

								if(newflag==1)
								{
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								Log.e("ST :You have entered Key Query-->:", String.valueOf(str1));
								//cKeyVal = "KeyQuery" + "#" + selctionfromQ;
								// String cKeyVal = "KeyQuery" + "#" + KeyQuery+"#"+mePort/5554;
								Log.e("ST KEYQUERY myport :", myPort);

								Context ctx1 = getContext();
								String valInKey = "";
								//  MatrixCursor mt = null;
								// String[] column = new String[]{"key", "value"};
								// mt = new MatrixCursor(column);


								String[] tempKeyAdd = str1.split("#");
								String selection = tempKeyAdd[1];
								String Qinfo = tempKeyAdd[2];

								File f = new File(getContext().getFilesDir().getAbsolutePath());
								FileInputStream fis = null;
								StringBuffer sb = new StringBuffer();
								try {
									fis = ctx1.getApplicationContext().openFileInput(selection);

									Log.e("File f:", f.toString());
									BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
									while ((valInKey = bis.readLine()) != null) {

										sb.append(valInKey);
									}

								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								String valuetobereturned = sb.toString();

								//mt = new MatrixCursor(column);
								//String row[] = new String[]{selection, valuetobereturned};
								//mt.addRow(row);
								// QueryFlag=1;
								Log.e("ST Before Query Flag:", String.valueOf(QueryFlag));
								queryAns = "reply" + "#" + QueryFlag + "#" + selection + "#" + valuetobereturned;

								// String finalQreplay="reply"+QueryFlag;
								//socket coonceton to change flags

								Socket socketR = null;
								try {
									socketR = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											(Integer.parseInt(Qinfo) * 2));
									DataOutputStream dos = new DataOutputStream(socketR.getOutputStream());
									Log.e("Msg to Qurrying avd is QueryFlag=1", queryAns);
									dos.writeUTF(queryAns);
								} catch (IOException e) {
									e.printStackTrace();
								}
								//
								Log.e("ST Query after Flag:", String.valueOf(QueryFlag));
								//  Log.e("Query target AVd mt.row:", String.valueOf(row));
								//  Log.e("Query target AVdmt normal", mt.toString());

								Log.e("Query target AVdppend(value2breturnd):", valuetobereturned);

							}//sync

						}
						else if(s1.equals("reply"))
						{
							//synchronized (this)//last change
							{
								if(newflag==1)
								{
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								//  queryAns = "reply"+"#"+QueryFlag+"#"+selection+"#"+valuetobereturned+"#"+"1";
								String[] lastArr = str1.split("#");
								socketreply=1;
								queryAns = lastArr[2] + "#" + lastArr[3];
								// socketreply= Integer.parseInt(lastArr[4]);

								Log.e("Updating value of QueryFlag in myport:from 0 to 1" + myPort, String.valueOf(QueryFlag));
								QueryFlag = 1;
								Log.e("ST QueryFlag set to 1 in" + myPort, String.valueOf(QueryFlag));
								//String row[] = new String[]{lastArr[2], lastArr[3]};
								//	mt.addRow(row);
								newflag=1;
							}
						}
						else if(s1.equals("StarKeyQuery")) {
							//	synchronized (this)
							{
								String[] lastArr1 = str1.split("#");
								String lastPor = lastArr1[2];
								int lastPorint = Integer.parseInt(lastArr1[2]) * 2;
								Context ctx1 = getContext();
								ArrayList st=new ArrayList();
								ArrayList st2=new ArrayList();
								String valInKey = "";
								{
									Log.e("in *:", starResults.toString());

									String[] files = ctx1.fileList();
									for (String file : files) {
										// FileInputStream fis=ctx1.openFileInput(file);
										//BufferedReader br=new BufferedReader(file);
										starResults.add(file);

										Log.e("STARKEYQ: KEYname of file:", file);
									}
									Log.e("STARKEYQ:list of files:results:", starResults.toString());
									Log.e("STARKEYQ:in *size of results:", String.valueOf(starResults.size()));

									File f = new File(getContext().getFilesDir().getAbsolutePath());
									for (int c = 0; c < starResults.size(); c++) {
										StringBuffer sb = new StringBuffer();
										FileInputStream fis = null;
										try {
											fis = ctx1.getApplicationContext().openFileInput(starResults.get(c));
											BufferedReader bis = new BufferedReader((new InputStreamReader(fis)));
											while ((valInKey = bis.readLine()) != null) {
												sb.append(valInKey);
												//			Log.e("STARKEYQ in *print c", String.valueOf(c));
												//			Log.e("STARKEYQ File f:", f.toString());
											}
										} catch (Exception e) {
											e.printStackTrace();
										}

										String valuetobereturned = sb.toString();
										Log.e("append(value2breturnd):", valuetobereturned);
										String data = starResults.get(c) + "#" + valuetobereturned;
										Log.e("STARKEYQ data ----------->", data);
										starShared.add(data);
										// String row[] = new String[]{starResults.get(c), valuetobereturned};
										//mt.addRow(row);
										// Log.e("mt.row:***:", String.valueOf(row));
										// Log.e("int C ***:", results.get(c) + "*" + c);

										//Log.e("mt ****", mt.toString());
									}
									Log.e("STARKEYQ starResults.toString ----------->", starResults.toString());
									Log.e("STARKEYQ starShared.toString ----------->", starShared.toString());



									// String finalQreplay="reply"+QueryFlag;
									//socket coonceton to change flags



									int sizeofstashared=starShared.size();
									int half=sizeofstashared/2;
									for(int y=0;y<half;y++)
									{
										String bell=starShared.get(y);
										st.add(bell);

									}
									for(int yz=half;yz<starShared.size();yz++)
									{
										String bell=starShared.get(yz);
										st2.add(bell);

									}

									Log.e("ST Before Star Query Flag:", String.valueOf(QueryFlagStar));

									//starQueryAns = "replyLast" + "#" + QueryFlagStar + "$" + String.valueOf(starShared);

									starQueryAns = "replyLast" + "#" + "1" + "$" + String.valueOf(st);
									String starQueryAns2 = "replyLast" + "#" + "1" + "$" + String.valueOf(st2);
									Socket socketR = null;
									try {
										socketR = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
												lastPorint);
											DataOutputStream dos = new DataOutputStream(socketR.getOutputStream());//object
										//ObjectOutputStream outputStream = new ObjectOutputStream(socketR.getOutputStream());

										Log.e(" starQueryAns = ;", starQueryAns);
										Log.e("starQueryAns2** =", starQueryAns2);
											dos.writeUTF(starQueryAns);
										dos.flush();
										dos.writeUTF(starQueryAns2);
										//outputStream.writeObject(starQueryAns);
										dos.flush();
									} catch (IOException e) {
										e.printStackTrace();
									}
									Log.e("ST Query after QueryFlagStar:", String.valueOf(QueryFlagStar));
								}

							}
						} else if(s1.equals("replyLast")) {

							//	synchronized (this)
							{
								//starQueryAns = "reply2"+"#"+"1"+"$"+String.valueOf(starShared);
								Log.e("replylast avd", myPort);
								//Log.e("reply str", String.valueOf(str1.length()));
								Log.e("str1", str1);
								String[] lastArr1 = str1.split("\\$");
								//	Log.e("lastArr1", lastArr1.toString());
								//	Log.e("sh", lastArr1[0]);
								String sh=lastArr1[0];
								String[] r=sh.split("#");
								int n1= Integer.parseInt(r[1]);
								Log.e("val n1:", String.valueOf(n1));

								sum=sum+n1;
								Log.e("sum ->:", String.valueOf(sum));
								String lastVal = lastArr1[1];
								//	Log.e("lastVal:", lastVal);


								String replace = lastVal.replace("[", "");
								//Log.e("replace:--------->", replace);
								String replace1 = replace.replace("]", "");
								//	Log.e("replace1:--------->", replace1);

								List<String> myList = new ArrayList<String>(Arrays.asList(replace1.split(",")));
								//Log.e("String convert", myList.toString());
								//ArrayList<String> al=new ArrayList<String>();
								// al.clear();
								//starShared.clear();

								for (int k = 0; k < myList.size(); k++)
								{
									starShared.add(myList.get(k).trim());
								}
//kl

								if(sum==2) {


								Log.e("Final starShared:", myPort + " " + starShared.toString());
								Log.e("Final starShared:", myPort + " " + String.valueOf(starShared.size()));

								Log.e("Updated value of QueryFlagStar in replylast:myport" + myPort, String.valueOf(QueryFlagStar));

									Log.e("sum 2:", String.valueOf(sum));
									QueryFlagStar = 1;
								}
							}
						}
					}//end of amruta else
					publishProgress(str1);
				}

			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();

			}


			return null;
		}

		protected void onProgressUpdate(String...strings) {

			//strRecieved = (msg) = "JoinPlease"+"#"+myPort;
			String strReceived = strings[0].trim();

			return;
		}
	}

}//provide class ends
class HashComp implements Comparator<String> {
	@Override
	public int compare(String s1, String s2) {

		String[] temp1=s1.split("#");
		String[] temp2=s2.split("#");

		String s11=temp1[0];
		String s22=temp2[0];

		return s11.compareTo(s22);
	}
}
