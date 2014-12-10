package com.d20pro.plugin.stock.pcgen;

import java.util.ArrayList;
import java.util.List;

import com.d20pro.plugin.api.ImportCreaturePlugin;
import com.mindgene.common.plugin.Factory;

/**
 * Factory for PCGen
 * 
 * @author devonjones
 */
public class CommandFactoryImpl implements Factory<ImportCreaturePlugin> {
	public List<ImportCreaturePlugin> getPlugins() {
		ArrayList<ImportCreaturePlugin> plugins = new ArrayList<ImportCreaturePlugin>();
		plugins.add(new CreatureImportPlugin_PCGen());
		return plugins;
	}
}
