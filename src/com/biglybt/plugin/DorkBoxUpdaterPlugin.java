/*
 * Copyright (C) Bigly Software.  All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.biglybt.plugin;

import java.io.File;

import com.biglybt.pif.PluginException;
import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.PluginManager;
import com.biglybt.pif.UnloadablePlugin;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManagerListener;
import com.biglybt.pif.update.UpdateException;
import com.biglybt.pif.update.UpdateInstaller;
import com.biglybt.pif.update.UpdateManager;
import com.biglybt.pif.utils.LocaleUtilities;

public class DorkBoxUpdaterPlugin
	implements UnloadablePlugin, UIManagerListener
{
	private PluginInterface pluginInterface;

	private File fileExistingJar;

	@Override
	public void unload()
			throws PluginException {

		if (pluginInterface == null) {
			return;
		}

		pluginInterface.getUIManager().removeUIListener(this);
	}

	@Override
	public void initialize(PluginInterface pluginInterface)
			throws PluginException {
		this.pluginInterface = pluginInterface;

		UpdateManager updateManager = pluginInterface.getUpdateManager();
		
		fileExistingJar = new File( updateManager.getInstallDir(), "dorkbox-systemtray.jar");
		
		if ( !fileExistingJar.exists()){
			
			return;
		}
		
		pluginInterface.getUIManager().addUIListener(this);
	}

	private void startUpgrade(UIInstance instance) {
		if (pluginInterface == null || fileExistingJar == null || !fileExistingJar.exists()) {
			return;
		}

		UpdateManager updateManager = pluginInterface.getUpdateManager();
		try {
			UpdateInstaller installer = updateManager.createInstaller();
			
			String target = new File(fileExistingJar.getParentFile(),fileExistingJar.getName() + ".obsolete" ).getAbsolutePath();
					
			installer.addMoveAction(fileExistingJar.getAbsolutePath(), target);

			showScriptAutoUpdateDialog(pluginInterface, instance);
			
		} catch (UpdateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void UIAttached(UIInstance instance) {
		if (instance.getUIType().equals(UIInstance.UIT_SWT)) {
			startUpgrade(instance);
		}
	}

	@Override
	public void UIDetached(UIInstance instance) {

	}

	private void showScriptAutoUpdateDialog(PluginInterface pluginInterface,
			UIInstance instance) {
		LocaleUtilities localeUtils = pluginInterface.getUtilities().getLocaleUtilities();
		instance.promptUser(
				localeUtils.getLocalisedMessageText("dorkboxupdater.update.title"),
				localeUtils.getLocalisedMessageText("dorkboxupdater.update.text"),
				new String[] {
					localeUtils.getLocalisedMessageText("UpdateWindow.restart"),
					localeUtils.getLocalisedMessageText("UpdateWindow.restartLater"),
				}, 0, result -> {
					if (result == 0) {
						try {
							PluginManager.restartClient();
						} catch (PluginException e) {
							e.printStackTrace();
						}
					}
				});
	}
}
