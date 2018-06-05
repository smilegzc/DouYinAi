/**
 * AiFace测试类
 */
public class Test {
	public static void main(String[] args) {
		AiFace face = new AiFace();
		for(int i = 1; i <= 6; i++) {
			String imgName = i + ".jpg";
			String imgDir = "E:/face/" + imgName;
			face.setImage(imgDir);
			
			if(face.getRet() != 0) {
				System.out.println("error:" + face.getRet());
				continue;
			}
			
			int faceSum = face.getFaceSum();
			int[][] faceSAB = face.getSAB();
			System.out.println("图片 " + imgName + " 人数:" + faceSum);
			for(int j = 0; j < faceSum; j++) {
				String sex = "女";
				if(faceSAB[j][0] > 50) {
					sex = "男";
				}
				String info = "性别：" + sex + " 年龄：" + faceSAB[j][1] + " 颜值:" + faceSAB[j][2];
				int s = j + 1;
				System.out.println("第" + s + "个人信息："+ "\n" + info + "\n");
			}
			
			System.out.println("**********华丽的分割线**********\n");
		}
	}
}