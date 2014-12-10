package com.d20pro.plugin.stock.pcgen;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.d20pro.plugin.api.CreatureImportServices;
import com.d20pro.plugin.api.ImportCreatureException;
import com.mindgene.d20.common.D20Rules;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.creature.attack.CreatureAttack;
import com.mindgene.d20.common.creature.attack.CreatureAttackDamage;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Acid;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Admantine;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Bash;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Chaotic;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Cold;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Cold_Iron;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Critical;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Electricity;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Evil;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Fire;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Good;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Lawful;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Magic;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Nonlethal;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Pierce;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Silver;
import com.mindgene.d20.common.creature.attack.CreatureAttackQuality_Slash;
import com.mindgene.d20.common.creature.attack.CreatureAttackStyle_1Hand;
import com.mindgene.d20.common.creature.attack.CreatureAttackType_Finesse;
import com.mindgene.d20.common.creature.attack.CreatureAttackType_Melee;
import com.mindgene.d20.common.creature.attack.CreatureAttackType_Range;
import com.mindgene.d20.common.creature.attack.CreatureAttackType_Thrown;
import com.mindgene.d20.common.creature.capability.CreatureCapability_SpellCaster;
import com.mindgene.d20.common.dice.Dice;
import com.mindgene.d20.common.game.creatureclass.CreatureClassBinder;
import com.mindgene.d20.common.game.creatureclass.CreatureClassNotInstalledException;
import com.mindgene.d20.common.game.creatureclass.GenericCreatureClass;
import com.mindgene.d20.common.game.feat.Feat_InitModifier;
import com.mindgene.d20.common.game.feat.GenericFeat;
import com.mindgene.d20.common.game.skill.GenericSkill;
import com.mindgene.d20.common.game.skill.GenericSkillTemplate;
import com.mindgene.d20.common.game.skill.MalformedSkillException;
import com.mindgene.d20.common.game.skill.SkillBinder;
import com.mindgene.d20.common.game.spell.SpellBinder;
import com.mindgene.d20.common.importer.ImportedSpell;

/**
 * Static container for logic to import from PCGen XML.
 * 
 * @author devonjones
 */
public class PCGenImportLogic {
	private PCGenImportLogic() {
		/** static only */
	}

	public static Node getChildNamed(Node node, String name, String path) throws ImportCreatureException {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals(name)) {
					return child;
				}
			}
		}
		if (null == path) {
			path = name;
		}
		throw new ImportCreatureException("PCGen file missing " + path + " tag.");
	}

	public static String getName(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node name = getChildNamed(basics, "name", "character/basics/name");
		return name.getTextContent();
	}

	public static String getSize(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node size = getChildNamed(basics, "size", "character/basics/size");
		Node longSize = getChildNamed(size, "long", "character/basics/size/long");
		return longSize.getTextContent();
	}

	public static String getAlignment(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node alignment = getChildNamed(basics, "alignment", "character/basics/alignment");
		Node longAlignment = getChildNamed(alignment, "long", "character/basics/alignment/long");
		return longAlignment.getTextContent();
	}

	public static Dimension getSpace(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node face = getChildNamed(basics, "face", "character/basics/face");
		Node ff = getChildNamed(face, "face", "character/basics/face/face");
		Node fs = getChildNamed(face, "short", "character/basics/face/short");
		int s1 = Integer.parseInt(ff.getTextContent().replace(" ft.", "")) / 5;
		int s2 = Integer.parseInt(fs.getTextContent().replace(" ft.", "")) / 5;
		return new Dimension(s1, s2);
	}

	public static int getReach(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node reach = getChildNamed(basics, "reach", "character/basics/reach");
		Node rr = getChildNamed(reach, "reach", "character/basics/reach/reach");
		return Integer.parseInt(rr.getTextContent().replace(" ft.", ""));
	}

	public static String getType(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node type = getChildNamed(basics, "type", "character/basics/type");
		return type.getTextContent();
	}

	public static String getExperience(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node experience = getChildNamed(basics, "experience", "character/basics/experience");
		Node current = getChildNamed(experience, "current", "character/basics/experience/current");
		return current.getTextContent();
	}

	public static int getWalkSpeed(Node root) throws ImportCreatureException {
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node moves = getChildNamed(basics, "move", "character/basics/move");
		for (Node move = moves.getFirstChild(); move != null; move = move.getNextSibling()) {
			if (move.getNodeType() == Node.ELEMENT_NODE && move.getNodeName().equals("move")) {
				Node name = getChildNamed(move, "name", "character/basics/name");
				if (name.getTextContent().equals("Walk")) {
					Node rate = getChildNamed(move, "rate", "character/basics/rate");
					return Integer.parseInt(rate.getTextContent().replace(" ft.", ""));
				}
			}
		}
		throw new ImportCreatureException("No Walking speed found");
	}

	public static int getAttribute(Node root, String abbrev) throws ImportCreatureException {
		Node abilities = getChildNamed(root, "abilities", "character/abilities");
		for (Node ability = abilities.getFirstChild(); ability != null; ability = ability.getNextSibling()) {
			if (ability.getNodeType() == Node.ELEMENT_NODE) {
				try {
					Node name = getChildNamed(ability, "name", "character/abilities/ability/name");
					Node short_name = getChildNamed(name, "short", "character/abilities/ability/name/short");
					String sn = short_name.getTextContent().toUpperCase();
					if (abbrev.toUpperCase().equals(sn)) {
						Node score = getChildNamed(ability, "score", "character/abilities/ability/name/score");
						return Integer.parseInt(score.getTextContent());
					}
				} catch (ImportCreatureException e) {
				}
			}
		}
		throw new ImportCreatureException("Attribute not found: " + abbrev);
	}

	public static int getSave(Node root, String abbrev) throws ImportCreatureException {
		Node savingThrows = getChildNamed(root, "saving_throws", "character/saving_throws");
		for (Node savingThrow = savingThrows.getFirstChild(); savingThrow != null; savingThrow = savingThrow
				.getNextSibling()) {
			if (savingThrow.getNodeType() == Node.ELEMENT_NODE) {
				try {
					Node name = getChildNamed(savingThrow, "name", "character/saving_throws/saving_throw/name");
					Node short_name = getChildNamed(name, "short", "character/saving_throws/saving_throw/name/short");
					String sn = short_name.getTextContent().toUpperCase();
					if (abbrev.toUpperCase().equals(sn)) {
						Node total = getChildNamed(savingThrow, "total", "character/saving_throws/saving_throw/total");
						String tv = total.getTextContent().replace("+", "");
						return Integer.parseInt(tv);
					}
				} catch (ImportCreatureException e) {
				}
			}
		}
		throw new ImportCreatureException("Saving Throw not found: " + abbrev);
	}

	public static int getHitPoints(Node root) throws ImportCreatureException {
		Node hitPoints = getChildNamed(root, "hit_points", "character/hit_points");
		Node points = getChildNamed(hitPoints, "points", "character/hit_points/points");
		return Integer.parseInt(points.getTextContent());
	}

	public static byte[] getArmorValues(CreatureTemplate ctr, Node root) throws ImportCreatureException {
		byte[] _ac = new byte[6];
		for (int i = 0; i < 6; i++) {
			_ac[i] = 0;
		}

		// "Natural", "Armor", "Shield", "Deflect", "Enhancement", "Dodge"
		Node armorClass = getChildNamed(root, "armor_class", "character/armor_class");
		Node natural = getChildNamed(armorClass, "natural", "character/armor_class/natural");
		_ac[0] += Byte.parseByte(natural.getTextContent());
		Node armorBonus = getChildNamed(armorClass, "armor_bonus", "character/armor_class/armor_bonus");
		_ac[1] = Byte.parseByte(armorBonus.getTextContent());
		Node shieldBonus = getChildNamed(armorClass, "shield_bonus", "character/armor_class/shield_bonus");
		_ac[2] = Byte.parseByte(shieldBonus.getTextContent());
		Node deflection = getChildNamed(armorClass, "deflection", "character/armor_class/deflection");
		_ac[3] += Byte.parseByte(deflection.getTextContent());
		Node classBonus = getChildNamed(armorClass, "class_bonus", "character/armor_class/class_bonus");
		_ac[4] += Byte.parseByte(classBonus.getTextContent());
		//Node sizeMod = getChildNamed(armorClass, "size_mod", "character/armor_class/size_mod");
		//_ac[4] += Byte.parseByte(sizeMod.getTextContent());
		Node misc = getChildNamed(armorClass, "misc", "character/armor_class/misc");
		_ac[4] += Byte.parseByte(misc.getTextContent());
		Node dodgeBonus = getChildNamed(armorClass, "dodge_bonus", "character/armor_class/dodge_bonus");
		_ac[5] += Byte.parseByte(dodgeBonus.getTextContent());
		//Node statMod = getChildNamed(armorClass, "stat_mod", "character/armor_class/stat_mod");
		//_ac[] = Byte.parseByte(statMod.getTextContent());
		return _ac;
	}

	public static int getMaxDex(Node root) throws ImportCreatureException {
		int maxDexMod = 0;
		Node armorClass = getChildNamed(root, "armor_class", "character/armor_class");
		Node maxDex = getChildNamed(armorClass, "max_dex", "character/armor_class/max_dex");
		String md = maxDex.getTextContent().replace("+", "");
		try {
			maxDexMod = Integer.parseInt(md);
		} catch (NumberFormatException nfe) {
		}
		return maxDexMod;
	}

	public static ArrayList<GenericCreatureClass> getClasses(CreatureClassBinder binder, CreatureTemplate ctr, Node root)
			throws ImportCreatureException {
		ArrayList<GenericCreatureClass> classList = new ArrayList<GenericCreatureClass>();
		Node basics = getChildNamed(root, "basics", "character/basics");
		Node classes = getChildNamed(basics, "classes", "character/basics/classes");
		for (Node cls = classes.getFirstChild(); cls != null; cls = cls.getNextSibling()) {
			if (cls.getNodeType() == Node.ELEMENT_NODE && cls.getNodeName().equals("class")) {
				Node name = getChildNamed(cls, "name", "character/basics/classes/class/name");
				String nameOfClass = name.getTextContent();
				Node lvl = getChildNamed(cls, "level", "character/basics/classes/class/level");
				String levelAsTxt = lvl.getTextContent();
				byte level = 1;
				try {
					level = Byte.parseByte(levelAsTxt);
				} catch (NumberFormatException nfe) {
					ctr.addToErrorLog("Illegal level value: " + levelAsTxt);
				}
				try {
					GenericCreatureClass aClass = new GenericCreatureClass(binder.accessClass(nameOfClass));
					aClass.setCreature(ctr);
					aClass.setLevel(level);
					classList.add(aClass);
				} catch (CreatureClassNotInstalledException cclnie) {
					ctr.addToErrorLog("Unable to import: " + nameOfClass + " " + level + " :" + cclnie.getMessage());
					defaultToFighter1(ctr, classList, binder, level);
				}
			}
		}
		return classList;
	}

	public static ArrayList<GenericFeat> getFeats(CreatureTemplate ctr, Node root) throws ImportCreatureException {
		ArrayList<GenericFeat> featList = new ArrayList<GenericFeat>();
		Node feats = getChildNamed(root, "feats", "character/feats");
		for (Node feat = feats.getFirstChild(); feat != null; feat = feat.getNextSibling()) {
			if (feat.getNodeType() == Node.ELEMENT_NODE && feat.getNodeName().equals("feat")) {
				Node name = getChildNamed(feat, "name", "character/feats/feat/name");
				String nameOfFeat = name.getTextContent();
				if (Feat_InitModifier.IMPROVED_INIT.equalsIgnoreCase(nameOfFeat)) {
					featList.add(Feat_InitModifier.buildStandard());
				} else {
					featList.add(new GenericFeat(nameOfFeat));
				}
			}
		}
		return featList;
	}

	public static ArrayList<GenericSkill> getSkills(CreatureImportServices svc, CreatureTemplate ctr, Node root)
			throws ImportCreatureException {
		SkillBinder binder = svc.accessSkills();
		ArrayList<GenericSkill> skillList = new ArrayList<GenericSkill>();
		Node skills = getChildNamed(root, "skills", "character/skills");
		for (Node skill = skills.getFirstChild(); skill != null; skill = skill.getNextSibling()) {
			if (skill.getNodeType() == Node.ELEMENT_NODE && skill.getNodeName().equals("skill")) {
				Node name = getChildNamed(skill, "name", "character/skills/skill/name");
				String skillName = name.getTextContent();
				Node ranks = getChildNamed(skill, "ranks", "character/skills/skill/ranks");
				Short skillRanks = ((Float) Float.parseFloat(ranks.getTextContent())).shortValue();
				Node misc = getChildNamed(skill, "misc_mod", "character/skills/skill/misc_mod");
				Short skillMisc = ((Float) Float.parseFloat(misc.getTextContent())).shortValue();
				GenericSkillTemplate skillTemplate = binder.accessSkill(skillName);
				if (null != skillTemplate) {
					skillList.add(new GenericSkill(skillTemplate, skillRanks, skillMisc));
				} else {
					try {
						skillTemplate = new GenericSkillTemplate(skillName);
						Node ability = getChildNamed(skill, "ability", "character/skills/skill/ability");
						String abilityName = ability.getTextContent();
						for (byte i = 0; i < D20Rules.Ability.NAMES.length; i++) {
							if (abilityName.equals(D20Rules.Ability.NAMES[i])) {
								skillTemplate.setAbility(i);
							}
						}
						skillList.add(new GenericSkill(skillTemplate, skillRanks, skillMisc));
						ctr.addToNotes("skill not found: " + skillName);
					} catch (MalformedSkillException mse) {
						ctr.addToNotes("skill not found: " + skillName);
					}
				}
			}
		}
		return skillList;
	}

	public static void importSpells(CreatureImportServices svc, CreatureTemplate ctr, Node root)
			throws ImportCreatureException {
		Node spells = getChildNamed(root, "spells", "character/spells");
		Node known_spells = getChildNamed(spells, "known_spells", "character/spells/known_spells");
		for (Node classNode = known_spells.getFirstChild(); classNode != null; classNode = classNode.getNextSibling()) {
			if (classNode.getNodeType() == Node.ELEMENT_NODE && classNode.getNodeName().equals("class")) {
				NamedNodeMap attrs = classNode.getAttributes();
				String spellListClass = attrs.getNamedItem("spelllistclass").getNodeValue();
				CreatureCapability_SpellCaster casting = ctr.extractSpellCasting(spellListClass.toLowerCase());
				ArrayList<ImportedSpell> spellList = getSpellsForClass(svc, ctr, root, classNode, spellListClass, null);
				casting.importSpellsKnown(spellList.toArray(new ImportedSpell[spellList.size()]));
				ArrayList<ImportedSpell> memorizedSpellList = new ArrayList<ImportedSpell>();
				for (int i = 0; i < spellList.size(); i++) {
					ImportedSpell is = spellList.get(i);
					if (is.getCastsLeft() > 0) {
						for (int j = 0; j < is.getCastsLeft(); j++) {
							memorizedSpellList.add(is);
						}
					}
				}
				try {
					casting.importSpellsMemorized(memorizedSpellList.toArray(new ImportedSpell[memorizedSpellList
							.size()]));
				} catch (Exception e) {
					ctr.addToErrorLog("Tried to import memorized spells on a class that does not support it: "
							+ spellListClass);
				}
			}
		}
	}

	public static ArrayList<ImportedSpell> getSpellsForClass(CreatureImportServices svc, CreatureTemplate ctr,
			Node root, Node classNode, String spellListClass, String domainName) throws ImportCreatureException {
		SpellBinder binder = svc.accessSpells();
		ArrayList<ImportedSpell> spellList = new ArrayList<ImportedSpell>();
		for (Node levelNode = classNode.getFirstChild(); levelNode != null; levelNode = levelNode.getNextSibling()) {
			if (levelNode.getNodeType() == Node.ELEMENT_NODE && levelNode.getNodeName().equals("level")) {
				NamedNodeMap attrs = levelNode.getAttributes();
				int level = Integer.parseInt(attrs.getNamedItem("number").getNodeValue());
				for (Node spellNode = levelNode.getFirstChild(); spellNode != null; spellNode = spellNode
						.getNextSibling()) {
					if (spellNode.getNodeType() == Node.ELEMENT_NODE && spellNode.getNodeName().equals("spell")) {
						Node nameNode = getChildNamed(spellNode, "name",
								"character/spells/known_spells/class/level/spell/name");
						String spellName = nameNode.getTextContent();
						Node bonusNode = getChildNamed(spellNode, "bonusspell",
								"character/spells/known_spells/class/level/spell/bonusspell");
						String bonusspell = bonusNode.getTextContent();
						int memorized = countSpellMemorized(root, spellListClass, spellName);
						if (domainName == null) {
							if (binder.hasSpell(spellName)) {
								ctr.addToNotes("spell not found: " + spellName);
							}
							if (!"**".equals(bonusspell) && "Cleric".equals(spellListClass)) {
								spellList.add(new ImportedSpell(spellName, level, memorized));
							}
						} else {
							Node sourceNode = getChildNamed(spellNode, "source",
									"character/spells/known_spells/class/level/spell/source");
							Node sourcelevelNode = getChildNamed(sourceNode, "sourcelevel",
									"character/spells/known_spells/class/level/spell/source/sourcelevel");
							String sourcelevel = sourcelevelNode.getTextContent();
							if ("**".equals(bonusspell) && sourcelevel.contains(domainName)) {
								spellList.add(new ImportedSpell(spellName, level, memorized));
							}
						}
					}
				}
			}
		}
		return spellList;
	}

	public static Integer countSpellMemorized(Node root, String className, String spellName)
			throws ImportCreatureException {
		Integer memed = 0;
		Node spells = getChildNamed(root, "spells", "character/spells");
		Node memorized_spells = getChildNamed(spells, "memorized_spells", "character/spells/memorized_spells");
		Node spellbook = null;
		try {
			spellbook = getChildNamed(memorized_spells, "spellbook", "character/spells/memorized_spells/spellbook");
		} catch (Exception e) {
			return memed;
		}
		for (Node classNode = spellbook.getFirstChild(); classNode != null; classNode = classNode.getNextSibling()) {
			if (classNode.getNodeType() == Node.ELEMENT_NODE && classNode.getNodeName().equals("class")) {
				NamedNodeMap attrs = classNode.getAttributes();
				String spellListClass = attrs.getNamedItem("spelllistclass").getNodeValue();
				if (spellListClass.equals(className)) {
					for (Node levelNode = classNode.getFirstChild(); levelNode != null; levelNode = levelNode
							.getNextSibling()) {
						if (levelNode.getNodeType() == Node.ELEMENT_NODE && levelNode.getNodeName().equals("level")) {
							for (Node spellNode = levelNode.getFirstChild(); spellNode != null; spellNode = spellNode
									.getNextSibling()) {
								if (spellNode.getNodeType() == Node.ELEMENT_NODE
										&& spellNode.getNodeName().equals("spell")) {
									Node nameNode = getChildNamed(spellNode, "name",
											"character/spells/memorized_spells/class/level/spell/name");
									String testName = nameNode.getTextContent();
									if (testName.equals(spellName)) {
										Node timesMem = getChildNamed(spellNode, "times_memorized",
												"character/spells/memorized_spells/class/level/spell/times_memorized");
										memed = Integer.parseInt(timesMem.getTextContent());
									}
								}
							}
						}
					}
				}
			}
		}
		return memed;
	}

	public static void importDomains(CreatureImportServices svc, CreatureTemplate ctr, Node root)
			throws ImportCreatureException {
		Map<String, List<ImportedSpell>> domains = new HashMap<String, List<ImportedSpell>>();
		Node domainsNode = null;
		try {
			domainsNode = getChildNamed(root, "domains", "character/domains");
		} catch (Exception e) {
			return;
		}
		for (Node domain = domainsNode.getFirstChild(); domain != null; domain = domain.getNextSibling()) {
			if (domain.getNodeType() == Node.ELEMENT_NODE && domain.getNodeName().equals("domain")) {
				Node nameNode = getChildNamed(domain, "name", "character/domains/domain/name");
				String domainName = nameNode.getTextContent();
				domains.put(domainName, getDomainSpells(svc, ctr, root, domainName));
			}
		}
		Node spells = getChildNamed(root, "spells", "character/spells");
		Node known_spells = getChildNamed(spells, "known_spells", "character/spells/known_spells");
		for (Node classNode = known_spells.getFirstChild(); classNode != null; classNode = classNode.getNextSibling()) {
			if (classNode.getNodeType() == Node.ELEMENT_NODE && classNode.getNodeName().equals("class")) {
				NamedNodeMap attrs = classNode.getAttributes();
				String spellListClass = attrs.getNamedItem("spelllistclass").getNodeValue();
				CreatureCapability_SpellCaster casting = ctr.extractSpellCasting(spellListClass.toLowerCase());
				try {
					casting.importSpellsDomain(domains);
				} catch (Exception e) {
				}
			}
		}
	}

	public static ArrayList<ImportedSpell> getDomainSpells(CreatureImportServices svc, CreatureTemplate ctr, Node root,
			String domainName) throws ImportCreatureException {
		ArrayList<ImportedSpell> spellList = new ArrayList<ImportedSpell>();
		Node spells = getChildNamed(root, "spells", "character/spells");
		Node known_spells = getChildNamed(spells, "known_spells", "character/spells/known_spells");
		for (Node classNode = known_spells.getFirstChild(); classNode != null; classNode = classNode.getNextSibling()) {
			if (classNode.getNodeType() == Node.ELEMENT_NODE && classNode.getNodeName().equals("class")) {
				NamedNodeMap attrs = classNode.getAttributes();
				String spellListClass = attrs.getNamedItem("spelllistclass").getNodeValue();
				spellList.addAll(getSpellsForClass(svc, ctr, root, classNode, spellListClass, domainName));
			}
		}
		return spellList;
	}

	private static void defaultToFighter1(CreatureTemplate ctr, ArrayList<GenericCreatureClass> classes,
			CreatureClassBinder binder, int level) {
		try {
			ctr.addToErrorLog("Defaulting to Fighter");
			GenericCreatureClass fighter = new GenericCreatureClass(binder.accessClass("Fighter"));
			fighter.setLevel((byte) level);
			fighter.setCreature(ctr);
			classes.add(fighter);
		} catch (CreatureClassNotInstalledException cclnie) {
			ctr.addToErrorLog("Fighter class not found, skipping class");
		}
	}

	public static void importAttacks(CreatureImportServices svc, CreatureTemplate ctr, Node root)
			throws ImportCreatureException {
		Node weapons = getChildNamed(root, "weapons", "character/weapons");
		importUnarmedAttacks(svc, ctr, weapons);
		importWeaponAttacks(svc, ctr, weapons, root);
	}

	public static void importUnarmedAttacks(CreatureImportServices svc, CreatureTemplate ctr, Node weapons)
			throws ImportCreatureException {
		Node nUnarmed = getChildNamed(weapons, "unarmed", "character/weapons/unarmed");
		Node nTotal = getChildNamed(nUnarmed, "total", "character/weapons/unarmed/total");
		Node nDamage = getChildNamed(nUnarmed, "damage", "character/weapons/unarmed/damage");
		Node nCrit = getChildNamed(nUnarmed, "critical", "character/weapons/unarmed/critical");
		CreatureAttack attack = new CreatureAttack();
		attack.setName("Unarmed");

		//Damage
		CreatureAttackDamage unarmedAttack = new CreatureAttackDamage();
		unarmedAttack.addQuality(new CreatureAttackQuality_Bash());
		String damage = nDamage.getTextContent();
		
		// Strip off modifiers, then add back only magic and feat modifiers
		String positiveOrNegative ="";
		if( damage.contains("+"))
			positiveOrNegative = "+";
		else if (damage.contains("-"))
			positiveOrNegative = "-";
		
		if (positiveOrNegative.length() > 0)
			damage = damage.substring(0,damage.indexOf(positiveOrNegative));
		
		try {
			if (damage.length() == 1) {
				unarmedAttack.setDice(new Dice(damage + "d1"));
			} else {
				unarmedAttack.setDice(new Dice(damage));
			}
		} catch (Exception e) {
			try {
				unarmedAttack.setDice(new Dice("1d0"));
			} catch (Exception ee) {
			} finally {
				ctr.addToErrorLog("Unable to set Damage: " + damage + " for unarmed attack");
			}
		}
		ArrayList<CreatureAttackDamage> damages = new ArrayList<CreatureAttackDamage>();
		damages.add(unarmedAttack);
		attack.setDamages(damages);
		
		//Crit
		String crit = nCrit.getTextContent();
		try {
			StringTokenizer sToke = new StringTokenizer(crit, "/x");
			String range = sToke.nextToken();
			String mult = sToke.nextToken();
			attack.setCritMinThreat(Byte.parseByte(range));
			attack.setCritMultiplier(Byte.parseByte(mult));
		} catch (Exception e) {
			ctr.addToErrorLog("Unable to set crit range: " + crit + " for unarmed attack");
		}

		attack.setStyle(new CreatureAttackStyle_1Hand());
		attack.assumeType(new CreatureAttackType_Melee());

		ctr.getAttacks().add(attack);
	}

	public static void importWeaponAttacks(CreatureImportServices svc, CreatureTemplate ctr, Node weapons, Node root)
			throws ImportCreatureException {
		for (Node nWeapon = weapons.getFirstChild(); nWeapon != null; nWeapon = nWeapon.getNextSibling()) {
			if (nWeapon.getNodeType() == Node.ELEMENT_NODE && nWeapon.getNodeName().equals("weapon")) {
				importWeaponAttack(svc, ctr, nWeapon, root);
			}
		}
	}

	public static void importWeaponAttack(CreatureImportServices svc, CreatureTemplate ctr, Node nWeapon, Node root)
			throws ImportCreatureException {
		Node nCommon = getChildNamed(nWeapon, "common", "character/weapons/weapon/common");
		Node nName = getChildNamed(nCommon, "name", "character/weapons/weapon/common/name");

		//To Hit
		Node nBaseHit = getChildNamed(nCommon, "basehit", "character/weapons/weapon/basehit");
		Node nMagic = getChildNamed(nCommon, "magic", "character/weapons/weapon/magic");
		Node nMagicHit = getChildNamed(nMagic, "hit", "character/weapons/weapon/magic/hit");
		Node nMagicDam = getChildNamed(nMagic, "damage", "character/weapons/weapon/magic/damage");
		Node nFeat = getChildNamed(nCommon, "feat", "character/weapons/weapon/feat");
		Node nFeatHit = getChildNamed(nFeat, "hit", "character/weapons/weapon/feat/hit");
		Node nFeatDam = getChildNamed(nFeat, "damage", "character/weapons/weapon/feat/damage");
		Node nTemplate = getChildNamed(nCommon, "template", "character/weapons/weapon/template");
		Node nTemplateHit = getChildNamed(nTemplate, "hit", "character/weapons/weapon/template/hit");
		Node nTemplateDam = getChildNamed(nTemplate, "damage", "character/weapons/weapon/template/damage");
		Node nDamageType = getChildNamed(nCommon, "type", "character/weapons/weapon/type");
		String total = nBaseHit.getTextContent().replace("+", "");
	
		if (total.equals("N/A")) {
			total = "0";
		}
		StringTokenizer sToke = new StringTokenizer(total, "/");
		while (sToke.hasMoreTokens()) {
			CreatureAttack attack = new CreatureAttack();
			if(nMagicHit.getTextContent().length()==0)
				nMagicHit.setTextContent("0");
			if(nFeatHit.getTextContent().length()==0)
				nFeatHit.setTextContent("0");
			if(nTemplateHit.getTextContent().length()==0)
				nTemplateHit.setTextContent("0");
			if(nMagicDam.getTextContent().length()==0)
				nMagicDam.setTextContent("0");
			if(nFeatDam.getTextContent().length()==0)
				nFeatDam.setTextContent("0");
			if(nTemplateDam.getTextContent().length()==0)
				nTemplateDam.setTextContent("0");
			
			
			short toHitTotal = Short.parseShort(nMagicHit.getTextContent().replace("+", "")); 
			toHitTotal+=  Short.parseShort(nFeatHit.getTextContent().replace("+", "")); 
			toHitTotal+= Short.parseShort(nTemplateHit.getTextContent().replace("+", ""));
			
			String toHit = sToke.nextToken();
			attack.setToHit(toHitTotal);

			Node nOutput = getChildNamed(nName, "output", "character/weapons/weapon/common/name/output");
			String name = nOutput.getTextContent().replace("*", "");
			attack.setName(name);

			//Damage
			CreatureAttackDamage attackDamage = new CreatureAttackDamage();
			Node nDamage = getChildNamed(nCommon, "damage", "character/weapons/weapon/damage");
			String damage = nDamage.getTextContent();
			try {
				// Strip off modifiers, then add back only magic and feat modifiers
				String positiveOrNegative ="";
				if( damage.contains("+"))
					positiveOrNegative = "+";
				else if (damage.contains("-"))
					positiveOrNegative = "-";
				
				if (positiveOrNegative.length() > 0)
					damage = damage.substring(0,damage.indexOf(positiveOrNegative));
				
				// Add on modifiers for magic, feats and templates
				short damageMod;
				String strDamageMod = "+";
				
				damageMod = (short) (Short.parseShort(nFeatDam.getTextContent()) + 
							Short.parseShort(nMagicDam.getTextContent()) +
							Short.parseShort(nTemplateDam.getTextContent()));
				
				if (damageMod < 0)
					strDamageMod = "-";
						
				if (damageMod != 0)
					damage = damage + strDamageMod + Short.toString(damageMod);
				
				if (damage.length() == 1) {
					attackDamage.setDice(new Dice(damage + "d1"));
				} else {
					attackDamage.setDice(new Dice(damage));
				}
				
				discoverMaterial( name, attackDamage );
				discoverDamageType( nDamageType.getTextContent(), attackDamage);
			} catch (Exception e) {
				try {
					attackDamage.setDice(new Dice("1d0"));
				} catch (Exception ee) {
				} finally {
					ctr.addToErrorLog("Unable to set Damage: " + damage + " for attack: " + name);
				}
			}
			ArrayList<CreatureAttackDamage> damages = new ArrayList<CreatureAttackDamage>();
			damages.add(attackDamage);
			attack.setDamages(damages);
			addMagicalEnhancementsIfExist(name.toLowerCase(),damages,attack);
			

			Node nCrit = getChildNamed(nCommon, "critical", "character/weapons/weapon/critical");
			try {
				Node nCritRange = getChildNamed(nCrit, "range", "character/weapons/weapon/critical/range");
				Node nCritMult = getChildNamed(nCrit, "multiplier", "character/weapons/weapon/critical/multiplier");
				String range = nCritRange.getTextContent();
				String mult = nCritMult.getTextContent();
				attack.setCritMinThreat(Byte.parseByte(range.substring(0, range.indexOf("-"))));
				attack.setCritMinThreat(Byte.parseByte(range));
				attack.setCritMultiplier(Byte.parseByte(mult));
			} catch (Exception e) {
				ctr.addToErrorLog("Unable to set crit range for attack: " + name);
			}

			//Fetch needed pieces off of equipment
			Node nLongName = getChildNamed(nName, "long", "character/weapons/weapon/common/name/long");
			Node nItem = findWeaponInItems(root, nLongName.getTextContent().replace("*", ""));
			if (nItem != null) {
				Node nType = getChildNamed(nItem, "type", "character/equipment/item/type");
				String type = nType.getTextContent();
				addTypes(attack, type);
			}
			ctr.getAttacks().add(attack);
		}
	}
	
	 public static void discoverDamageType( String damageType, CreatureAttackDamage damage )
    {
	   	if(damageType.contains("S"))
    	  damage.addQuality( new CreatureAttackQuality_Slash() );
    	if(damageType.contains("B"))
      	  damage.addQuality( new CreatureAttackQuality_Bash() );
    	if(damageType.contains("P"))
      	  damage.addQuality( new CreatureAttackQuality_Pierce() );   		
    }
	
	public static void discoverMaterial( String damageType, CreatureAttackDamage damage )
	{
    	damageType = damageType.toLowerCase();
    	

    	if(damageType.contains("+"))
    	  damage.addQuality( new CreatureAttackQuality_Magic() );    	
    	if(damageType.contains("mithral") || damageType.contains("silver"))
    	  damage.addQuality( new CreatureAttackQuality_Silver() );
    	if(damageType.contains("adamantine"))
      	  damage.addQuality( new CreatureAttackQuality_Admantine() );
    	if(damageType.contains("cold iron") || damageType.contains("nexavaran steel") )
      	  damage.addQuality( new CreatureAttackQuality_Cold_Iron() );   		
    }
	
	public static void addMagicalEnhancementsIfExist( String enhancementName, ArrayList damages, CreatureAttack attack )
    {
	  if( enhancementName.length() > 0 )
      {
    	    	
    	try
        {
		  CreatureAttackDamage damage = new CreatureAttackDamage();
    	
		  if(enhancementName.contains("flaming"))
		  {
		    damage.setDice( new Dice( "1d6" ) );
    		damage.addQuality( new CreatureAttackQuality_Fire() );
    		damages.add( damage );
    		if(enhancementName.contains("burst"))
    		{
    		  CreatureAttackDamage burstDamage = new CreatureAttackDamage();
    		  String numDice;
    		  numDice = Byte.toString((byte)(attack.getCritMultiplier()-1)); 
    		  burstDamage.setDice( new Dice( numDice + "d10" ) );
    		  burstDamage.addQuality( new CreatureAttackQuality_Fire() );
    		  burstDamage.addQuality( new CreatureAttackQuality_Critical() );
    		  damages.add( burstDamage );
    		}
		  }
		  
		  if(enhancementName.contains("frost") || enhancementName.contains("icy burst"))
		  {
		    damage.setDice( new Dice( "1d6" ) );
    		damage.addQuality( new CreatureAttackQuality_Cold() );
    		damages.add( damage );
    		if(enhancementName.contains("burst"))
    		{
    		  CreatureAttackDamage burstDamage = new CreatureAttackDamage();
    		  String numDice;
    		  numDice = Byte.toString((byte)(attack.getCritMultiplier()-1)); 
    		  burstDamage.setDice( new Dice( numDice + "d10" ) );
    		  burstDamage.addQuality( new CreatureAttackQuality_Cold() );
    		  burstDamage.addQuality( new CreatureAttackQuality_Critical() );
    		  damages.add( burstDamage );
    		}
		  }
		  
		  if(enhancementName.contains("shock"))
		  {
		    damage.setDice( new Dice( "1d6" ) );
    		damage.addQuality( new CreatureAttackQuality_Electricity() );
    		damages.add( damage );
    		if(enhancementName.contains("burst"))
    		{
    		  CreatureAttackDamage burstDamage = new CreatureAttackDamage();
    		  String numDice;
    		  numDice = Byte.toString((byte)(attack.getCritMultiplier()-1)); 
    		  burstDamage.setDice( new Dice( numDice + "d10" ) );
    		  burstDamage.addQuality( new CreatureAttackQuality_Electricity() );
    		  burstDamage.addQuality( new CreatureAttackQuality_Critical() );
    		  damages.add( burstDamage );
    		}
		  }
		  
		  if(enhancementName.contains("corrosive"))
		  {
		    damage.setDice( new Dice( "1d6" ) );
    		damage.addQuality( new CreatureAttackQuality_Acid() );
    		damages.add( damage );
    		if(enhancementName.contains("burst"))
    		{
    		  CreatureAttackDamage burstDamage = new CreatureAttackDamage();
    		  String numDice;
    		  numDice = Byte.toString((byte)(attack.getCritMultiplier()-1)); 
    		  burstDamage.setDice( new Dice( numDice + "d10" ) );
    		  burstDamage.addQuality( new CreatureAttackQuality_Acid() );
    		  burstDamage.addQuality( new CreatureAttackQuality_Critical() );
    		  damages.add( burstDamage );
    		}
		  }
		  
		  if(enhancementName.contains("vicious"))
		  {
		    damage.setDice( new Dice( "2d6" ) );
    		damage.addQuality( new CreatureAttackQuality_Magic() );
    		damages.add( damage );
		  }
    	  
		  if(enhancementName.contains("merciful"))
		  {
		    damage.setDice( new Dice( "1d6" ) );
    		damage.addQuality( new CreatureAttackQuality_Nonlethal() );
    		damages.add( damage );
		  }
		  
		  if(enhancementName.contains("holy"))
		    ((CreatureAttackDamage)damages.get(0)).addQuality( new CreatureAttackQuality_Good() );
		    
		  if(enhancementName.contains("unholy"))
  		    ((CreatureAttackDamage)damages.get(0)).addQuality( new CreatureAttackQuality_Evil() );
  		  
		  if(enhancementName.contains("axiomatic"))
  		    ((CreatureAttackDamage)damages.get(0)).addQuality( new CreatureAttackQuality_Lawful() );
  		  
		  if(enhancementName.contains("anarchic"))
  		    ((CreatureAttackDamage)damages.get(0)).addQuality( new CreatureAttackQuality_Chaotic() );
        }
		catch( Exception ee )
    	{
    	}	    	
      }
        
    }

	public static void addTypes(CreatureAttack attack, String type) {
		if (type.indexOf("FINESSEABLE") > -1) {
			attack.assumeType(new CreatureAttackType_Finesse());
		} else if (type.indexOf("MELEE") > -1) {
			attack.assumeType(new CreatureAttackType_Melee());
		} else if (type.indexOf("THROWN") > -1) {
			attack.assumeType(new CreatureAttackType_Thrown());
		} else if (type.indexOf("RANGED") > -1) {
			attack.assumeType(new CreatureAttackType_Range());
		} else {
			attack.assumeType(new CreatureAttackType_Melee());
		}
	}

	public static void addTypes(CreatureAttackDamage attackDamage, String type) {
		if (type.contains("BLUDGEONING")) {
			attackDamage.addQuality(new CreatureAttackQuality_Bash());
		}
		if (type.contains("SLASHING")) {
			attackDamage.addQuality(new CreatureAttackQuality_Slash());
		}
		if (type.contains("PIERCING")) {
			attackDamage.addQuality(new CreatureAttackQuality_Pierce());
		}
		if (type.contains("MITHRAL") || type.contains("SILVER")) {
			attackDamage.addQuality(new CreatureAttackQuality_Silver());
		}
		if (type.contains("ADAMANTINE")) {
			attackDamage.addQuality(new CreatureAttackQuality_Admantine());
		}
		if (type.contains("COLDIRON")) {
			attackDamage.addQuality(new CreatureAttackQuality_Cold_Iron());
		}
		if (type.contains("MAGIC")) {
			attackDamage.addQuality(new CreatureAttackQuality_Magic());
		}
	}

	public static Node findWeaponInItems(Node root, String longname) throws ImportCreatureException {
		Node nEquipment = getChildNamed(root, "equipment", "character/equipment");
		for (Node nItem = nEquipment.getFirstChild(); nItem != null; nItem = nItem.getNextSibling()) {
			if (nItem.getNodeType() == Node.ELEMENT_NODE && nItem.getNodeName().equals("item")) {
				Node nLongName = getChildNamed(nItem, "longname", "character/equipment/item/longname");
				if (longname.toLowerCase().equals(nLongName.getTextContent().toLowerCase())) {
					return nItem;
				}
			}
		}
		return null;
	}
}
