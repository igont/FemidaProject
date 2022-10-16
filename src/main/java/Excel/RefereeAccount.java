package main.java.Excel;

public class RefereeAccount // Аккаунт определенного человека
{
	public int id;
	public String fName;
	public String sName;
	public String mName;
	public int birth_year;
	public String city;
	public String phone;
	public float calc_points;
	public String category;
	public String clubType;
	public String clubName;


	public RefereeAccount()
	{
		final String notSpecified = "???";
		fName = notSpecified;
		sName = notSpecified;
		mName = notSpecified;
		birth_year = 0;
		city = notSpecified;
		phone = notSpecified;
		calc_points = 0;
		category = notSpecified;
		clubType = notSpecified;
		clubName = notSpecified;
		id = -1;
	}

	@Override
	public String toString()
	{
		return "RefereeAccount{" + "id=" + id + ", fName='" + fName + '\'' + ", sName='" + sName + '\'' + ", mName='" + mName + '\'' + ", birth_year=" + birth_year + ", city='" + city + '\'' + ", phone='" + phone + '\'' + ", calc_points=" + calc_points + ", category='" + category + '\'' + ", clubType='" + clubType + '\'' + ", clubName='" + clubName + '\'' + '}';
	}
}
