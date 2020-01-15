package org.abpass.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;

public class ReloadSceneCssService extends ScheduledService<Void> {
	private final Map<Scene, List<String>> targets;
	
	public ReloadSceneCssService() {
		targets = new WeakHashMap<Scene, List<String>>();
	}
	
	public void addSceneCss(Scene scene, String css) {
		if (! Platform.isFxApplicationThread()) {
			throw new RuntimeException("only add in fx application thread");
		}
		
		targets.computeIfAbsent(scene, (k) -> new ArrayList<String>()).add(css);
		scene.getStylesheets().add(css);
	}
	
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Platform.runLater(() -> {
					for (var p : targets.entrySet()) {
						var sheets = p.getKey().getStylesheets();
						for (var css : p.getValue()) {
							sheets.remove(css);
							sheets.add(css);
						}
					}
				});
				return null;
			}
		};
	}

}
