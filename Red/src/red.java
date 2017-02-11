import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class red {
	public static int counter = 0;

	public static void main(String[] args) {
// 8  and 10 for counter less than 60000
		// 7 and 9 for counter less between 60000 and 120000
		// 5 and 6 for counter greater than 120
		
		String PROXY = "d09.cs.ucr.edu:3128";
		
		org.openqa.selenium.Proxy proxy = null;
		DesiredCapabilities cap = null;
		WebDriver driver = null;

		proxy = new org.openqa.selenium.Proxy();
		proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
		cap = new DesiredCapabilities();
		cap.setCapability(CapabilityType.PROXY, proxy);
		driver = new FirefoxDriver(cap);
		

		 //WebDriver driver = new FirefoxDriver();
		 
		 
		String prefix = "http://webcache.googleusercontent.com/search?q=cache:";

		java.sql.Timestamp timeStampDate = null;
		String cache_time = null;

		long waitDuration = 0;

		// initialize first element

		Connection myConn;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/homeDB", "root", "home123");

			myConn.setAutoCommit(false);
			int zipcode = 0;
			Statement myStmt = myConn.createStatement();

			ResultSet myRs2 = null;

			int Redfin_Estimate = 0;
			Statement myStmt2 = myConn.createStatement();
			WebElement ele = null;
			while (counter < 20000) {

				if (counter % 40 == 0) {
					driver.close();

					PROXY = "d09.cs.ucr.edu:3128";
					proxy = new org.openqa.selenium.Proxy();
					proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
					cap = new DesiredCapabilities();
					cap.setCapability(CapabilityType.PROXY, proxy);
					System.out.println(PROXY);
					System.out.println("****************************************");
					driver = new FirefoxDriver(cap);

				} else if (counter % 20 == 0) {

					driver.close();

					PROXY = "d10.cs.ucr.edu:3128";
					proxy = new org.openqa.selenium.Proxy();
					proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
					cap = new DesiredCapabilities();
					cap.setCapability(CapabilityType.PROXY, proxy);
					System.out.println(PROXY);
					System.out.println("****************************************");
					driver = new FirefoxDriver(cap);
				}
				

				Double r = 183553 * Math.random() + 1;

				counter++;

				ResultSet rs = myStmt
						.executeQuery("select zip from z2 where cumulative_sum< " + r + "order by id desc limit 1");

				if (rs.next()) {
					zipcode = rs.getInt(1);

				}

				//

				// WebDriver driver = new FirefoxDriver();
				

				myRs2 = myStmt2.executeQuery("select id , url from redfin where Redfin_Estimate=-1 and zip="
						+ zipcode + " limit 1");

				if (myRs2.next()) {

					String urll = myRs2.getString("url").trim();
					String[] uu = urll.split("http");

					String uuu = "https" + uu[1];

					String u = prefix + uuu;

					int id = myRs2.getInt("id");

					if (u.isEmpty()) {
						Redfin_Estimate = 0;
					} else {
						driver.get(u);
						List<WebElement> elems = driver.findElements(By.className("avmValue"));
						if (elems.size() == 0) {
							Redfin_Estimate = 0;
							// System.out.printf("zerooo id %d", id);
						} else {
							ele = driver.findElement(By.className("avmValue"));
							if (ele.getText().isEmpty()) {
								Redfin_Estimate = 0;

							}

							else {

								WebElement ele2 = driver.findElement(By.id("google-cache-hdr"));
								String t = ele2.getText();

								String[] d = t.split("It is a snapshot of the page as it appeared on ");

								String[] d1 = d[1].split(" GMT");

								String[] d2 = d1[0].split("2016");

								cache_time = d2[0].trim() + " 2016";

								DateFormat formatter;
								formatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
								Date date;
								try {
									date = (Date) formatter.parse(cache_time);

									timeStampDate = new Timestamp(date.getTime());

								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								String ans = ele.getText();
								NumberFormat format = NumberFormat.getCurrencyInstance();
								Number number;
								try {
									number = format.parse(ans);

									String nn = number.toString();
									Redfin_Estimate = Integer.parseInt(nn);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}
					}

					if (Redfin_Estimate != 0) {
						System.out.print("Find itttttttttttttttttttttt ");
						System.out.print(Redfin_Estimate);
						System.out.print(" id  ");
						System.out.println(id);

					} else {
						System.out.println("not found id " + id + "---counter: " + counter);
					}

					if (Redfin_Estimate != 0) {
						String query2 = "UPDATE redfin SET Redfin_Estimate=?, crawl_time=? where id=" + id;

						PreparedStatement ps2 = myConn.prepareStatement(query2);
						ps2.setInt(1, Redfin_Estimate);
						if (!cache_time.isEmpty()) {
							ps2.setTimestamp(2, timeStampDate);
						}
						ps2.executeUpdate();

						myConn.commit();
						ps2.close();
					} else {
						String query2 = "UPDATE redfin SET Redfin_Estimate=? where id=" + id;

						PreparedStatement ps2 = myConn.prepareStatement(query2);
						ps2.setString(1, null);
						ps2.executeUpdate();

						myConn.commit();
						ps2.close();
					}

					waitDuration = (long) ((20 * Math.random() + 10) * 1000);
					if (counter % 10 == 0) {

						Thread.sleep(20000);
						System.out.println("20000 sleep");
					}

					Thread.sleep(waitDuration);
					System.out.print("waitDuration  ");
					System.out.print(waitDuration);
				}

			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (NoSuchElementException e) {
			System.out.println("NoSuchElementException");
		}

	}

}
