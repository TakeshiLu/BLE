import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class CMDWang {
	public static String cmd;
	public static Process process;
	public static InputStream inputStream;
	public static OutputStream outputStream;
	public static byte[] recvBuffer;
	public static String BLETargetAddress;
	public static String[] sendAddress;
	public static String[] recieveAddress;

	public CMDWang() {
		cmd = "/bin/bash";
		recvBuffer = new byte[512];
		recieveAddress = new String[] { "19b10023-e8f2-537e-4f6c-d104768a1214", "19b10024-e8f2-537e-4f6c-d104768a1214",
				"19b10025-e8f2-537e-4f6c-d104768a1214", "19b10026-e8f2-537e-4f6c-d104768a1214",
				"19b10027-e8f2-537e-4f6c-d104768a1214" };
		sendAddress = new String[] { "0x000d", "0x000f", "0x0011", "0x0013", "0x0015" };
		try {
			process = Runtime.getRuntime().exec(cmd);
			inputStream = process.getInputStream();
			outputStream = process.getOutputStream();
			BLETargetAddress = "98:4f:ee:0f:a6:b9";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		CMDWang C = new CMDWang();
		String result = "";
		Scanner sc = new Scanner(System.in);

		try {
			C.sendMessage("HI");
			C.receiveMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String[] getExecutedResult() {
		String result = "";
		int len;
		try {
			len = this.inputStream.read(recvBuffer, 0, 512);
			// System.out.println("length of result : " + len);
			if (len > 0) {
				result = new String(recvBuffer, 0, len);
				// System.out.println(result);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		String[] output = result.split("\n");
		return output;
	}

	public void setBLEAddress(String address) {
		this.BLETargetAddress = address;
	}

	public void execute(String command) {
		try {
			this.outputStream.write((command).getBytes());
			this.outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void BLEConnect() throws InterruptedException {
		String[] connectResult;
		boolean isConnectSucceesful = false;
		System.out.println("["+BLETargetAddress+"] "+"Connect to device ...");

		// System.out.println("excute gatttool -b " + BLETargetAddress + " -I");
		this.execute("gatttool -b " + BLETargetAddress + " -I\n");
		Thread.sleep(600);
		// printExecutedResult(getExecutedResult());

		while (!isConnectSucceesful) {
			this.execute("connect\n");
			Thread.sleep(800);
			connectResult = getExecutedResult();
			// printExecutedResult(connectResult);
			for (String s : connectResult) {
				// System.out.println("String income " + i + ":" + s + " endl "
				// + i);
				// test = s.getBytes();
				// for(byte b:test){
				// System.out.print(b+",");
				// }
				// System.out.println();
				if (s.endsWith("Connection successful")) {// have some problem,
															// just print string
															// in
															// bytes.
					isConnectSucceesful = true;
					System.out.println("["+BLETargetAddress+"] "+"Connected");
				}
			}
			if (!isConnectSucceesful) {
				System.out.println("[" + BLETargetAddress + "] " + "Connection Failure, Retrying...");
			}
		}

	}

	public void BLEDisconnect() throws InterruptedException {
		System.out.println("[" + BLETargetAddress + "] " + "Disconnected");
		this.execute("disconnect\n");
		Thread.sleep(200);
		// printExecutedResult(getExecutedResult());

		// System.out.println("excute exit");
		this.execute("exit\n");
		Thread.sleep(200);
		// printExecutedResult(getExecutedResult());
	}

	public void printExecutedResult(String[] result) {
		for (String s : result) {
			System.out.println(s);
		}
	}

	public void sendMessage(String msg) throws InterruptedException {
		byte data[] = msg.getBytes();
		// System.out.println(msg.length());

		if (msg.length() <= 5) {

			BLEConnect();
			System.out.println("[" + BLETargetAddress + "] " + "Sending message :" + msg);
			for (int i = 0; i < msg.length(); i++) {
				// System.out.println("excute char-write-cmd " +
				// sendAddress[i]);
				this.execute("char-write-cmd " + sendAddress[i] + " " + this.decimal2hex(data[i]) + " \n");
				Thread.sleep(100);
				// printExecutedResult(getExecutedResult());
			}

			// System.out.println("excute char-write-cmd 0x000b");
			this.execute("char-write-cmd 0x000b " + (msg.length() + 30) + " \n");
			Thread.sleep(100);
			// printExecutedResult(getExecutedResult());
			System.out.println("[" + BLETargetAddress + "] " + "Message \"" + msg + "\" " + "successful sended");

			BLEDisconnect();
		} else {
			System.out.println("[" + BLETargetAddress + "] " + "send too large msg.");
		}

	}

	public String receiveMessage() throws InterruptedException {
		// receive message char by char from arduino101,and combine to a string
		BLEConnect();
		String msg = "";
		int dataLength = lengthOfRieceveData();
		System.out.println("[" + BLETargetAddress + "] " + "Receving message" + " ...");
		for (int i = 0; i < dataLength; i++) {
			// System.out.println("excute char-read-uuid " + recieveAddress[i]);
			this.execute("char-read-uuid " + recieveAddress[i] + "\n");
			Thread.sleep(1800);
			String m = parseMessage(getExecutedResult());
			// System.out.println("I got data:" + m);
			msg = msg + m;
		}
		System.out.println("[" + BLETargetAddress + "] " + "Receive message : " + msg);

		BLEDisconnect();
		return msg;
	}

	public String parseMessage(String[] messagePackage) {
		// pick up the message(char) from income string

		String msg = "";
		messagePackage = messagePackage[1].split(" ");
		char c = (char) Integer.parseInt(messagePackage[5], 16);
		// System.out.println("data length: " + (Integer.parseInt(result[5]) -
		// 30));

		return msg + c;

	}

	private int lengthOfRieceveData() throws InterruptedException {
		// read length characteristic from arduino 101
		// System.out.println("excute char-read-uuid
		// 19b10022-e8f2-537e-4f6c-d104768a1214");
		this.execute("char-read-uuid 19b10022-e8f2-537e-4f6c-d104768a1214" + "\n");
		Thread.sleep(1800);
		String[] result = getExecutedResult();
		result = result[1].split(" ");

		// System.out.println("data length: " + (Integer.parseInt(result[5]) -
		// 30));

		return Integer.parseInt(result[5]) - 30;

	}

	public static String decimal2hex(int d) {
		// transfer decimal number to hex string

		String digits = "0123456789ABCDEF";
		if (d <= 0)
			return "0";
		int base = 16; // flexible to change in any base under 16
		String hex = "";
		while (d > 0) {
			int digit = d % base; // rightmost digit
			hex = digits.charAt(digit) + hex; // string concatenation
			d = d / base;
		}
		return hex;
	}
}
