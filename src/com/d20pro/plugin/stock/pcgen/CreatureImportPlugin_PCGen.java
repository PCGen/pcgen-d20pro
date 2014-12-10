package com.d20pro.plugin.stock.pcgen;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.d20pro.plugin.api.CreatureImportServices;
import com.d20pro.plugin.api.ImportCreatureException;
import com.d20pro.plugin.api.ImportCreaturePlugin;
import com.d20pro.plugin.api.ImportMessageLog;
import com.d20pro.plugin.api.XMLToDocumentHelper;
import com.d20pro.plugin.api.XMLToDocumentHelperStrategy;
import com.mindgene.common.util.FileFilterForExtension;
import com.mindgene.d20.common.D20Rules;
import com.mindgene.d20.common.creature.CreatureSpeeds;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.game.creatureclass.CreatureClassBinder;
import com.mindgene.d20.common.game.creatureclass.GenericCreatureClass;
import com.mindgene.d20.common.game.feat.GenericFeat;
import com.mindgene.d20.common.game.skill.GenericSkill;

/**
 * Import PCGen XML into Creatures.
 * 
 * @author devonjones
 */
public class CreatureImportPlugin_PCGen implements ImportCreaturePlugin, XMLToDocumentHelperStrategy {
	private static String _gameSystem = "3.5";

	public CreatureImportPlugin_PCGen() {
		_gameSystem = "3.5";
	}

	public String getPluginName() {
		return "PCGen";
	}

	public java.util.List<CreatureTemplate> parseCreatures(CreatureImportServices svc, ImportMessageLog log,
			Document doc) throws ImportCreatureException {
		ArrayList<CreatureTemplate> creatures = new ArrayList<CreatureTemplate>();
		Element root = doc.getDocumentElement();
		checkSignature(root);
		determineGameSystem(root);
		if (root.getNodeType() == Node.ELEMENT_NODE && "character".equals(root.getNodeName())) {
			creatures.add(parseCreature(svc, _gameSystem, root));
		}
		return creatures;
	}

	private static void checkSignature(Element root) throws ImportCreatureException {
		String name = root.getTagName();
		if (!(name.equals("character"))) {
			throw new ImportCreatureException("Not a PCGen character file");
		}
	}

	private static String determineGameSystem(Node child) {
		return "3.5";
	}

	private static CreatureTemplate parseCreature(CreatureImportServices svc, String gameSystem, Node characterNode)
			throws ImportCreatureException {
		CreatureTemplate ctr = new CreatureTemplate();
		ctr.setGameSystem(gameSystem);
		ctr.setModuleName("PCGen");
		ctr.setNotes("Imported from PCGen (" + gameSystem + ") on " + DateFormat.getDateInstance().format(new Date())
				+ "\n");
		parse(svc, ctr, null, characterNode);
		// Build the notes tab by combining notes, abilities, and error log
		ctr.buildFullNotes();
		return ctr;
	}

	private static void parse(CreatureImportServices svc, CreatureTemplate ctr, String domain, Node node)
			throws ImportCreatureException {
		parseBasics(ctr, node);
		parseAttributes(ctr, node);
		parseSaves(ctr, node);
		parseHitPoints(ctr, node);
		parseClasses(svc, ctr, node);
		parseArmor(ctr, node);
		parseFeats(ctr, node);
		parseSkills(svc, ctr, node);
		PCGenImportLogic.importSpells(svc, ctr, node);
		PCGenImportLogic.importAttacks(svc, ctr, node);
		PCGenImportLogic.importDomains(svc, ctr, node);
	}

	private static void parseBasics(CreatureTemplate ctr, Node node) throws ImportCreatureException {
		String name = PCGenImportLogic.getName(node);
		if (null == name) {
			throw new ImportCreatureException("missing name");
		}
		ctr.setName(name);
		String size = PCGenImportLogic.getSize(node);
		ctr.setSize(D20Rules.Size.getID(size));
		String align = PCGenImportLogic.getAlignment(node);
		ctr.setAlignment(align);
		ctr.setFace(PCGenImportLogic.getSpace(node));
		ctr.setReach((byte) (PCGenImportLogic.getReach(node) / 5));
		int speed = PCGenImportLogic.getWalkSpeed(node);
		ctr.accessSpeeds().assignLegacySpeed(CreatureSpeeds.feetToSquares(speed));
		ctr.setType(PCGenImportLogic.getType(node));
		ctr.setExperiencePoints(PCGenImportLogic.getExperience(node));
	}

	private static void parseClasses(CreatureImportServices svc, CreatureTemplate ctr, Node node)
			throws ImportCreatureException {
		CreatureClassBinder binder = svc.accessClasses();
		ArrayList<GenericCreatureClass> classes = PCGenImportLogic.getClasses(binder, ctr, node);
		ctr.getClasses().assignClasses(classes);
	}

	private static void parseAttributes(CreatureTemplate ctr, Node node) throws ImportCreatureException {
		for (byte i = 0; i < D20Rules.Ability.NAMES.length; i++) {
			String name = D20Rules.Ability.NAMES[i];
			int score = PCGenImportLogic.getAttribute(node, name);
			ctr.setAbility(i, (byte) score);
		}
	}

	private static void parseSaves(CreatureTemplate ctr, Node node) throws ImportCreatureException {
		for (byte i = 0; i < D20Rules.Save.NAMES.length; i++) {
			String name = D20Rules.Save.NAMES[i];
			int score = PCGenImportLogic.getSave(node, name);
			ctr.setSave(i, (byte) score);
		}
	}

	private static void parseHitPoints(CreatureTemplate ctr, Node node) throws ImportCreatureException {
		int value = PCGenImportLogic.getHitPoints(node);
		ctr.setHP((short) value);
		ctr.setHPMax((short) value);
	}

	private static void parseArmor(CreatureTemplate ctr, Node node) throws ImportCreatureException {
		ctr.setAC(PCGenImportLogic.getArmorValues(ctr, node));
		ctr.setMaxDexBonus((short) PCGenImportLogic.getMaxDex(node));
	}

	private static void parseFeats(CreatureTemplate ctr, Node node) throws ImportCreatureException {
		ArrayList<GenericFeat> feats = PCGenImportLogic.getFeats(ctr, node);
		ctr.getFeats().setFeats((GenericFeat[]) feats.toArray(new GenericFeat[0]));
	}

	private static void parseSkills(CreatureImportServices svc, CreatureTemplate ctr, Node node)
			throws ImportCreatureException {
		ArrayList<GenericSkill> skills = PCGenImportLogic.getSkills(svc, ctr, node);
		ctr.getSkills().setSkills(skills.toArray(new GenericSkill[skills.size()]));
	}

	@Override
	public java.util.List<CreatureTemplate> importCreatures(CreatureImportServices svc, ImportMessageLog log)
			throws ImportCreatureException {
		java.util.List<File> files = svc.chooseFiles(this);
		java.util.List<CreatureTemplate> creatures = new XMLToDocumentHelper().convert(svc, log, files, this);
		return creatures;
	}

	@Override
	public FileFilterForExtension getPluginFileFilter() {
		return new FileFilterForExtension("xml", "PCGen output");
	}

}
