import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class AiBot {
	//private final String imageName = "girl.png";
	private Random rand = new Random();
	
	private final int BEAUTY_THRESHOLD = 80; //颜值界限
	//模拟翻页的初始坐标X、Y和滑动距离L
	private final int nextX = 540;
	private final int nextY = 965;
	private final int nextL = 500;
	//点赞坐标
	private final int likeX = 1000;
	private final int likeY = 1083;
	//关注坐标
	private final int followX = 990;
	private final int followY = 950;
	
	//执行命令
	private void executeCommand(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			process.waitFor();
			String str;
			while((str = br.readLine()) != null) {
				System.out.println(str);
			}
		} catch(IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	//获取屏幕截图，返回截图路径
	public String getScreenShot() {
		String[] getScreenCommand = {"adb shell screencap -p /sdcard/girl.png",
				"adb pull /sdcard/girl.png E:/face"}; //截图，并将图片传到本地
		for(String cmd: getScreenCommand) {
			executeCommand(cmd);
		}
		
		return "E:/face/girl.png";
	}
	//颜值检测，返回值为0-100之间，值越大，颜值越高
	private int[] foundGirl(String imageDir) {
		int[] face = new int[2];
		AiFace faceInfo = new AiFace();
		faceInfo.setImage(imageDir);
		int faceSum = faceInfo.getFaceSum();
		if(faceSum != 1) {
			face[0] = -1;
			face[1] = -1;
			return face;
		}
		face[0] = faceInfo.getSex(0);
		face[1] = faceInfo.getBeauty(0);
		return face;
	}
	
	public void touchScreen(String imageDir) {
		//如果颜值大于设定值则点赞并关注
		int[] girlInfo = foundGirl(imageDir);
		//判断为图片中为单人、女孩、颜值大于设定值则点赞并关注
		if(girlInfo[0] >= 0 && girlInfo[0] < 50 && girlInfo[1] > BEAUTY_THRESHOLD) {
			String likeComm = String.format("adb shell input tap %s %s",
					likeX + rand.nextInt(10), likeY + rand.nextInt(10));
			executeCommand(likeComm);
			//Thread.sleep(500);
			
			String followComm = String.format("adb shell input tap %s %s",
					followX + rand.nextInt(10), followY + rand.nextInt(10));
			executeCommand(followComm);
			System.out.println("发现漂亮小姐姐，已点赞并关注。");
			//Thread.sleep(500);
		}
		
		//翻页
		String nextComm = String.format("adb shell input swipe %s %s %s %s %s",
				nextX, nextY + nextL, nextX, nextY, 200);
		executeCommand(nextComm);
		//Thread.sleep(500);
	}
	
	public static void main(String[] args) {
		AiBot bot = new AiBot();
		while(true) {
			String imageDir = bot.getScreenShot();
			bot.touchScreen(imageDir);
		}
	}
}