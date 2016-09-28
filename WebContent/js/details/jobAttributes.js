
$(document).ready(function(){
    'use strict';
    var jobSpaceId=getParameterByName('id');
    var rootJobSpaceId=getParameterByName('id');
    var jsTree=makeSpaceTree("#exploreList");
    // Initialize the jstree plugin for the explorer list
    var jobId = $('#data').data('jobid');
    var spaceExplorerJsonData = getSpaceExplorerJsonData(jobId);
    initSpaceExplorer(rootJobSpaceId, spaceExplorerJsonData);
	setupChangeTimeButton();
});

function getSpaceExplorerJsonData(jobId) {
    'use strict';
    var url = starexecRoot+"services/space/" +jobId+ "/jobspaces/true";
    return {
        "ajax" : {
            "url" : url, // Where we will be getting json data from
            "data" : function (n) {
                return {
                    id : (n.attr ? n.attr("id") : 0)
                }; // What the default space id should be
            }
        }
    };
}
function setupChangeTimeButton() {


	$(".changeTime").button({
		icons: {
			primary: "ui-icon-refresh"
		}
	});

	var isWallclock = true;
	$('.cpuSum').hide();


	var toggleTime = function() {
		if (isWallclock) {
			$('.changeTime .ui-button-text').html('use wallclock time');
			isWallclock = false;
			$('.cpuSum').show();
			$('.wallclockSum').hide();
		} else {
			isWallclock = true;
			$('.changeTime .ui-button-text').html('use CPU time');
			$('.wallclockSum').show();
			$('.cpuSum').hide();
		}
	}


	$(".changeTime").click(toggleTime);
		
}


function initSpaceExplorer(rootJobSpaceId, spaceExplorerJsonData) {
    // Set the path to the css theme for the jstree plugin

    $.jstree._themes = starexecRoot+"css/jstree/";

    // Initialize the jstree plugin for the explorer list
    /*$("#exploreList").bind("loaded.jstree", function() {
        log("exploreList tree has finished loading.");
        $("#exploreList").jstree("select_node", ".rootNode");
    })*/
    $("#exploreList").jstree({
        "json_data" : spaceExplorerJsonData,
        "themes" : {
            "theme" : "default",
            "dots" : true,
            "icons" : true
        },
        "types" : {
            "max_depth" : -2,
            "max_children" : -2,
            "valid_children" : [ "space" ],
            "types" : {
                "space" : {
                    "valid_children" : [ "space" ],
                    "icon" : {
                        "image" : starexecRoot+"images/jstree/db.png"
                    }
                }
            }
        },
        "ui" : {
            "select_limit" : 1,
            "selected_parent_close" : "select_parent",
            "initially_select" : [ "#"+rootJobSpaceId ]
        },
        "plugins" : [ "types", "themes", "json_data", "ui", "cookies"] ,
        "core" : { animation : 200 }
    }).bind("select_node.jstree", function (event, data) {
        // Change the page to the appropriate jobspace.
    }).on( "click", "a", function (event, data) {
        event.preventDefault();  // This just disable's links in the node title
    });
    log("Initialized exploreList tree.");

}