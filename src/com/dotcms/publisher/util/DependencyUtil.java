package com.dotcms.publisher.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dotcms.publisher.business.PublishQueueElement;
import com.dotcms.publisher.pusher.PushPublisherConfig;
import com.dotcms.publishing.PublisherConfig;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.MultiTree;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.factories.MultiTreeFactory;
import com.dotmarketing.portlets.containers.model.Container;
import com.dotmarketing.portlets.folders.business.FolderAPI;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.portlets.templates.model.Template;
import com.liferay.portal.model.User;

public class DependencyUtil {
	
	public static void setDependencies(PushPublisherConfig config, User user) throws DotDataException {
		List<PublishQueueElement> assets = config.getAssets();
		Set<String> htmlPages = new HashSet<String>();
		
		for (PublishQueueElement asset : assets) {
			if(asset.getType().equals("htmlpage")) {
				htmlPages.add(asset.getAsset());
			}
		}
		config.setHTMLPages(htmlPages);
		setHTMLPagesDependencies(htmlPages, config, user);
	}
	
	public static void setContentletDependencies() {
		
	}
	
	public static void setHTMLPagesDependencies(Set<String> pages, PushPublisherConfig config, User user) {
		try {
			IdentifierAPI idenAPI = APILocator.getIdentifierAPI();
			FolderAPI folderAPI = APILocator.getFolderAPI();
			Set<String> folders = new HashSet<String>();
			
			for (String page : pages) {
				Identifier iden = idenAPI.find(page);
				Folder folder = folderAPI.findFolderByPath(iden.getParentPath(), iden.getHostId(), user, false);
				folders.add(folder.getInode());
				setHTMLPageContentsAndStructures(page, config, user);
			}
			
			config.setFolders(folders);
			
			
		} catch (DotSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DotDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setHTMLPageContentsAndStructures(String id, PushPublisherConfig config, User user) {
		try {
			HTMLPage workingPage = APILocator.getHTMLPageAPI().loadWorkingPageById(id, user, false);
			HTMLPage livePage = APILocator.getHTMLPageAPI().loadLivePageById(id, user, false);
			// working template working page
			Template workingTemplateWP = APILocator.getTemplateAPI().findWorkingTemplate(workingPage.getTemplateId(), user, false);
			// live template working page
			Template liveTemplateWP = APILocator.getTemplateAPI().findLiveTemplate(workingPage.getTemplateId(), user, false);
			// live template live page
			Template liveTemplateLP = APILocator.getTemplateAPI().findLiveTemplate(livePage.getTemplateId(), user, false);
			
			List<Container> containers = new ArrayList<Container>();
			Set<String> containersSet = new HashSet<String>();
			
			containers.addAll(APILocator.getTemplateAPI().getContainersInTemplate(workingTemplateWP, user, false));
			containers.addAll(APILocator.getTemplateAPI().getContainersInTemplate(liveTemplateWP, user, false));
			containers.addAll(APILocator.getTemplateAPI().getContainersInTemplate(liveTemplateLP, user, false));
			Set<String> structuresSet = new HashSet<String>();
			Set<String> contentSet = new HashSet<String>();
			
			for (Container container : containers) {
				containersSet.add(container.getInode());
				structuresSet.add(container.getStructureInode());
				List<MultiTree> treeList = MultiTreeFactory.getMultiTree(container.getIdentifier());
				
				for (MultiTree mt : treeList) {
					String contentIdentifier = mt.getChild();
					contentSet.add(contentIdentifier);
				}
			}

			config.setStructures(structuresSet);
			config.setContents(contentSet);
			
		} catch (DotSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DotDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
