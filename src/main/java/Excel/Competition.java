package main.java.Excel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Competition // Полное описание соревнования для одного человека (Будет генерироваться на основе Глобальных соревнований)
{
	public String participantFName;
	public String participantSName;
	public String participantMName;
	public Date date;
	public String city;
	public String competitionName;
	public String position;
	public float grade;
	public String carpetChief;
	public String carpetNumber;
	public int id;

	public String firstBigLetter(String s) // Названия с большой буквы
	{
		s = (Character.toUpperCase(s.charAt(0)) + s.substring(1)).trim();
		return s.replaceAll("[\s]+", " ");
	}

	Competition()
	{
		participantFName = "???";
		participantSName = "???";
		participantMName = "???";
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 1900);
		date = calendar.getTime();

		city = "???";
		competitionName = "???";
		position = "???";
		grade = 0f;
		carpetNumber = "???";
		carpetChief = "???";
		id = -1;
	}

	public void print()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		System.out.printf("| %-25s", participantFName + " " + participantMName);
		System.out.printf("| %-15s", formatter.format(date));
		System.out.printf("| %-35s", city);
		System.out.printf("| %-90s", competitionName);
		System.out.printf("| %-25s", position);
		System.out.printf("| %-10s", grade);
		System.out.printf("| %-5s | \n", carpetNumber);
	}

	public void printHead()
	{
		System.out.printf(ExcelSQLTemp.DELIMETR);
		System.out.printf("| %-25s", "Участник");
		System.out.printf("| %-15s", "Дата");
		System.out.printf("| %-35s", "Город");
		System.out.printf("| %-90s", "Название соревнования");
		System.out.printf("| %-25s", "Должность");
		System.out.printf("| %-10s", "Очки");
		System.out.printf("| %-5s |\n", "Ковер");
		System.out.printf(ExcelSQLTemp.DELIMETR);
	}

	public void setParticipantFName(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.length() == 1) s += ".";
		s = firstBigLetter(s);

		this.participantFName = s;
	}

	public void setParticipantSName(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.length() == 1) s += ".";
		s = firstBigLetter(s);

		this.participantSName = s;
	}

	public void setParticipantMName(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.length() == 1) s += ".";
		s = firstBigLetter(s);

		this.participantMName = s;
	}

	public void setDate(Date d)
	{
		this.date = d;
	}

	public void setCity(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.length() == 1) s += ".";
		s = firstBigLetter(s);

		this.city = s;
	}

	public void setCompetitionName(String s)
	{
		if(s.length() == 0) s = "???";
		s = firstBigLetter(s);
		s = s.replaceAll("по АРБ", "АРБ");
		s = s.replaceAll("по Абсолютно реальному бою", "АРБ");
		s = s.replaceAll("АРБ", "по АРБ");

		s = s.replaceAll("ЮНЫЙ АРМЕЕЦ по АРБ", "ЮНЫЙ АРМЕЕЦ");
		s = s.replaceAll("ЮНЫЙ АРМЕЕЦ", "ЮНЫЙ АРМЕЕЦ по АРБ");

		s = s.replaceAll("федерации по АРБ", "федерации АРБ");


		this.competitionName = s;
	}

	public void setPosition(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.length() == 1) s += ".";
		s = s.replaceAll("\"", "");

		if(s.toLowerCase().contains("бок")) s = "Боковой судья";
		if(s.toLowerCase().contains("рук")) s = "Руководитель ковра";
		if(s.toLowerCase().contains("хр")) s = "Хронометрист";
		if(s.toLowerCase().contains("сек")) s = "Хронометрист";
		if(s.toLowerCase().contains("глав")) s = "Главный судья соревнований";

		s = firstBigLetter(s);

		this.position = s;
	}

	public void setGrade(float f)
	{
		this.grade = f;
	}

	public void setCarpetChief(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.length() == 1) s += ".";
		s = firstBigLetter(s);

		this.carpetChief = s;
	}

	public void setCarpetNumber(String s)
	{
		if(s.length() == 0) s = "???";
		if(s.equals("0")) s = "???";

		this.carpetNumber = s;
	}
}