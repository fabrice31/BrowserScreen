package jenkins.plugins.browserScreen;

import hudson.Extension;
import hudson.model.*;
import hudson.tasks.junit.*;
import hudson.Util;
import hudson.views.ListViewColumn;
import hudson.model.Descriptor.FormException;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.FormFieldValidator;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import javax.servlet.ServletException;
import java.math.BigDecimal;
import java.io.IOException;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;
import org.apache.commons.collections.CollectionUtils;

public class BrowserScreenView extends ListView {
    private final int millisecondsInAMinute = 60000;
    private final double minutesInAnHour = 60.0;

    @DataBoundConstructor
    public BrowserScreenView(String name) {
        super(name);
    }

  @Extension
  public static final class BrowserScreenViewDescriptor extends ViewDescriptor {

    @Override
    public String getDisplayName() {
      return "Browser Screen View by Fabrice";
    }

  }

	public String debug;
	public String title;
	public String browsersList;
	public String testsList;
	private String[][] resultList;
	// private String tableHTML;

	// public String getResultList() { return getResults();}
	public String getTableHTML() {
		this.resultList = getResults();
		return getTable();
	}
	
	
    @Override
    protected void submit(StaplerRequest req) throws ServletException,
          Descriptor.FormException, IOException {
		super.submit(req);

		this.browsersList = req.getParameter("browsersList");
		this.testsList = req.getParameter("testsList");
		this.title = req.getParameter("title");

		this.debug = "";

		// calculation
	}

	public String[][] getResults() {
		String[] tests = this.testsList.split(",");
		String[] browsers = this.browsersList.split(",");
		String[][] results = new String[tests.length][browsers.length];
		// list of all jobs
		Hudson hudson = Hudson.getInstance();
        List<Job> jobs = hudson.getAllItems(Job.class);

		int b,t;
		boolean found;
		for(t = 0; t < tests.length; t++) {
			for(b = 0; b < browsers.length; b++) {

				// Checking only the wanted job
				String pattern = tests[t].trim() + ".*" + browsers[b].trim() + ".*?";
				found = false;
				for(Job job:jobs) {
					if (!found && job.getName().toString().matches(pattern)) {
						if (job.getLastBuild() != null) {
							if (job.getLastBuild().isBuilding()) {
								results[t][b] = "build";
							} else if (jobIsFailed(job)) {
								results[t][b] = "error";
							} else {
								results[t][b] = "valid";
							}
						} else {
							results[t][b] = "noresult";
						}
						found = true;
					}
				}
				// case of job not existant
				if (!found) {
					results[t][b] = "notest";
				}
			}
		}
		
		return results;
	}

	private Boolean jobIsFailed (Job job) {
		return !(
			job.getLastBuild().getBuildStatusSummary().message.toString().equalsIgnoreCase("stable")
			|| job.getLastBuild().getBuildStatusSummary().message.toString().equalsIgnoreCase("back to normal")
		);
	}
	
	private String getTable() {
		String table = "";
		String[] tests = this.testsList.split(",");
		String[] browsers = this.browsersList.split(",");
		int b,t;
		
		Map<String, String> icons = new HashMap<String, String>();
		icons.put("build", "<i class=\"icon icon-repeat\"></i>");
		icons.put("error", "<i class=\"icon icon-remove\"></i>");
		icons.put("valid", "<i class=\"icon icon-ok\"></i>");
		icons.put("noresult", "&nbsp;");
		icons.put("notest", "&nbsp;");
		

		table = "<table>";
		// entete
		table += "<thead><tr><th></th>";
		for(b = 0; b < browsers.length; b++) {
			table += "<th>"+ browsers[b] +"</th>";
		}
		table += "</tr></thead>";
		
		// body
		for(t = 0; t < tests.length; t++) {
			table += "<tr><th>" + tests[t] + "</th>";
			for(b = 0; b < browsers.length; b++) {
				table += "<td class=\""+this.resultList[t][b]+"\">"
					+ icons.get(this.resultList[t][b])
					+"</td>";
			}
			table += "</tr>";
		}

		// fermeture
		table += "</table>";
		
		return table;
	}
}
