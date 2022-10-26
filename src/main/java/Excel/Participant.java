package main.java.Excel;

import main.java.Excel.SQLPack.SQLReal;

import java.sql.SQLException;

public class Participant // Результаты судьи за определенное соревнование
{
	public Participant()
	{
		id = -1;
		fName = "???";
		sName = "???";
		mName = "???";
		grade = 0;
		position = "???";
		comment = "???";
		carpet = "???";
	}
	public Participant(Competition competition) throws SQLException
	{
		new Participant();

		if(competition.carpetNumber != "0")
			carpet = competition.carpetNumber;

		grade = competition.grade;

		if(competition.carpetChief.length() > 0)
			carpetChief = competition.carpetChief;

		fName = competition.participantFName;
		sName = competition.participantSName;
		mName = competition.participantMName;

		if(competition.position.length() > 0)
			position = competition.position;

		comment = "???";
		id = SQLReal.getRefereeIDByFullName(fName, sName);
	}


	public int id;
	public String fName;
	public String sName;
	public String mName;
	public float grade;
	public String position;
	public String comment;
	public String carpet;
	public String carpetChief;

	public void setFName(String fName)
	{
		this.fName = fName;
	}

	public void setSName(String sName)
	{
		this.sName = sName;
	}

	public void setMName(String mName)
	{
		this.mName = mName;
	}

	public void setGrade(float grade)
	{
		this.grade = grade;
	}

	public void setPosition(String position)
	{
		this.position = position;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public void setCarpet(String carpet)
	{
		this.carpet = carpet;
	}

	public void setCarpetChief(String carpetChief)
	{
		this.carpetChief = carpetChief;
	}

	@Override
	public String toString()
	{
		return String.format("Referee №%s Name: %s %s Grade: %s Position: %s Carpet: %s Comment: %s \n", id, fName, mName, grade, position, carpet, comment);
	}
}
