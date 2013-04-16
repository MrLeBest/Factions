package com.massivecraft.factions.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.massivecraft.factions.ConfServer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionColl;
import com.massivecraft.factions.Perm;
import com.massivecraft.mcore.cmd.req.ReqHasPerm;
import com.massivecraft.mcore.util.Txt;


public class CmdFactionsList extends FCommand
{
	
	public CmdFactionsList()
	{
		this.addAliases("ls", "list");
		
		this.addOptionalArg("page", "1");
		
		this.addRequirements(ReqHasPerm.get(Perm.LIST.node));
	}

	@Override
	public void perform()
	{
		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(ConfServer.econCostList, "to list the factions", "for listing the factions")) return;
		
		ArrayList<Faction> factionList = new ArrayList<Faction>(FactionColl.get().getAll());

		factionList.remove(FactionColl.get().getNone());
		// TODO: Add flag SECRET To factions instead.
		//factionList.remove(Factions.i.getSafeZone());
		//factionList.remove(Factions.i.getWarZone());
		
		// Sort by total followers first
		Collections.sort(factionList, new Comparator<Faction>(){
			@Override
			public int compare(Faction f1, Faction f2) {
				int f1Size = f1.getFPlayers().size();
				int f2Size = f2.getFPlayers().size();
				if (f1Size < f2Size)
					return 1;
				else if (f1Size > f2Size)
					return -1;
				return 0;
			}
		});

		// Then sort by how many members are online now
		Collections.sort(factionList, new Comparator<Faction>(){
			@Override
			public int compare(Faction f1, Faction f2) {
				int f1Size = f1.getFPlayersWhereOnline(true).size();
				int f2Size = f2.getFPlayersWhereOnline(true).size();
				if (f1Size < f2Size)
					return 1;
				else if (f1Size > f2Size)
					return -1;
				return 0;
			}
		});
		
		ArrayList<String> lines = new ArrayList<String>();

/*		// this code was really slow on large servers, getting full info for every faction and then only showing 9 of them; rewritten below
		lines.add(Txt.parse("<i>Factionless<i> %d online", Factions.i.getNone().getFPlayersWhereOnline(true).size()));
		for (Faction faction : factionList)
		{
			lines.add(Txt.parse("%s<i> %d/%d online, %d/%d/%d",
				faction.getTag(fme),
				faction.getFPlayersWhereOnline(true).size(),
				faction.getFPlayers().size(),
				faction.getLandRounded(),
				faction.getPowerRounded(),
				faction.getPowerMaxRounded())
			);
		}
		
		sendMessage(Txt.getPage(lines, this.argAsInt(0, 1), "Faction List"));
 */

		factionList.add(0, FactionColl.get().getNone());

		final int pageheight = 9;
		int pagenumber = this.argAsInt(0, 1);
		int pagecount = (factionList.size() / pageheight) + 1;
		if (pagenumber > pagecount)
			pagenumber = pagecount;
		else if (pagenumber < 1)
			pagenumber = 1;
		int start = (pagenumber - 1) * pageheight;
		int end = start + pageheight;
		if (end > factionList.size())
			end = factionList.size();

		lines.add(Txt.titleize("Faction List "+pagenumber+"/"+pagecount));

		for (Faction faction : factionList.subList(start, end))
		{
			if (faction.isNone())
			{
				lines.add(Txt.parse("<i>Factionless<i> %d online", FactionColl.get().getNone().getFPlayersWhereOnline(true).size()));
				continue;
			}
			lines.add(Txt.parse("%s<i> %d/%d online, %d/%d/%d",
				faction.getTag(fme),
				faction.getFPlayersWhereOnline(true).size(),
				faction.getFPlayers().size(),
				faction.getLandRounded(),
				faction.getPowerRounded(),
				faction.getPowerMaxRounded())
			);
		}

		sendMessage(lines);
	}
}