package main.java.Excel;

import main.java.Excel.SQLPack.SQLReal;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GlobalCompetition
{
	public GlobalCompetition()
	{
		participants = new ArrayList<>();
		name = "???";
		city = "???";
		date = new Date();
	}

	public GlobalCompetition(Competition competition) throws SQLException
	{
		name = competition.competitionName;
		date = competition.date;
		city = competition.city;

		participants = new ArrayList<>();
		participants.add(new Participant(competition));
	}

	public String name;
	public String city;
	public Date date;
	public List<Participant> participants;

	public String setName(String name)
	{
		if(this.name.isEmpty())
		{
			this.name = name;
			return "Имя соревнования изменено на:\n" + name;
		}
		else
		{
			this.name = name;
			return "Имя соревнования установлено:\n" + name;
		}
	}

	public String setCity(String city)
	{
		if(this.name.isEmpty())
		{
			this.city = city;
			return "Город изменен на:\n" + city;
		}
		else
		{
			this.city = city;
			return "Город соревнований задан:\n" + city;
		}
	}

	public String setDate(Date date)
	{
		if(this.name.isEmpty())
		{
			this.date = date;
			return "Дата проведения соревнований изменена на:\n" + date;
		}
		else
		{
			this.date = date;
			return "Дата проведения соревнований задана:\n" + date;
		}
	}

	public void addParticipant(int id, float grade, String position, String carpet, String comment,String carpetChief) throws SQLException
	{
		RefereeAccount refereeAccount = new RefereeAccount();
		try
		{
			refereeAccount = SQLReal.getRefereeByID(id);
		}
		catch(PSQLException e)
		{
			System.out.println("Аккаунт с таким ID не обнаружен: [" + id + "]");
			return;
		}
		Participant participant = new Participant();

		participant.fName = refereeAccount.fName;
		participant.sName = refereeAccount.sName;
		participant.mName = refereeAccount.mName;

		participant.grade = grade;
		participant.position = position;
		participant.carpet = carpet;
		participant.comment = comment;
		participant.id = id;
		participant.carpetChief = carpetChief;

		participants.add(participant);
	}
}
