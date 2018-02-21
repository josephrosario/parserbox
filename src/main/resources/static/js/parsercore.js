	/**
	 * Main parser javascript class
	 **/
    function ParserCore (callBack) {
    
    	this.parsingTemplate;
		this.parsingFilters;
		this.parsingFileInfo;
		this.documentKey;
		
		this.colcount = 0;
        this.markers = 0;
       	this.ids = 0;
       	this.callBack = callBack;
       	this.mouseUpTimout = null;
       	
       	this.dirty = false;
       	this.MaskSelectionStart;
       	this.mainInterval;
       	this.userActive = false;
       	
       	this.intervalSeconds = 15;
       	
       	this.cancelAsyncTask = false;
       	
	 
	    /**
	  	 * Sets the dirty switch when a value has changed.
	  	 * This will be used by the main interval (loop) process
	  	 * to determine if it should call the server to save the project automatically.
	  	 **/
	    this.setDirty = function(dirty) {
	        this.dirty = dirty;
	    }
	    this.isDirty = function() {
	        return this.dirty;
	    }
	
	    /**
	  	 * This method tells us the user is in the work area
	  	 * possibly set or adjusting markers.
	  	 * Therefore we should attempt to delay an server calls.
	  	**/
	    this.setUserIsActive = function() {
	        this.userActive = true;
	    }
	
	  	this.setMainInterval = function(secondsParam) {
	  		var helper = this;
	  		var secs = secondsParam ? secondsParam : this.intervalSeconds;
	  		var ms = secs * 1000;
	        var params = {
	            "transaction": "project_changed"
	        };
	        
	       	if (this.mainInterval) {
	            window.clearInterval(this.mainInterval);
	            helper.mainInterval = null;
	        };
	        
	   		this.mainInterval = window.setInterval(function() {
	   			if (helper.userActive) {
	   				helper.userActive = false;
	   				return;
	   			}
	   			if (helper.isDirty()) {
	            	helper.doCallBack(params);
	   				helper.setDirty(false);
	            }       
	   		}, ms);
	   }
	    
		this.doCallBack = function(params) {
			if (this.callBack) {
				this.callBack(params);
			}
		}
			
	    this.showColumnMarker = function(parsingFilter, bypassEvent) {
	    	var helper = this;
	 		if (parsingFilter == null) return;
	 		
	        var idParam = parsingFilter.Id;
	              
	        var el = document.getElementById(idParam);
	        if (el != null){
	            // --- alert('Marker already exists for ' + titleParam + '.  Double click on existing marker to remove.');
	            return;
	        }
	        
	        var titleParam = parsingFilter.Name;
	        var cX = parsingFilter.ColumnMarkerLeft__c;
	        var cY = parsingFilter.ColumnMarkerTop__c;
	        var widthParam = parsingFilter.ColumnMarkerWidth__c;
	        var heightParam = parsingFilter.ColumnMarkerHeight__c;
	        var filterType = parsingFilter.FilterType__c;
	        
	        var titleColor = this.getDefaultMarkerTitleColor(filterType);
	        var bodyColor = this.getDefaultMarkerBodyColor(filterType);
	        
	        var colwidth = widthParam ? widthParam : 100;
	        var colheight = heightParam ? heightParam : 200;
	        if (cX == null || cX == 0) cX = (this.colcount * (colwidth+5));
	        if (cY == null || cY == 0) cY = 1;
	        if (this.colcount >= 5) { this.colcount = 0;} else { this.colcount++; }
	        this.markers++;
	        var div = document.createElement('div');
	        div.id = idParam;
	        div.name = idParam;
	        div.style.position = 'absolute';
	        div.style.left = cX + 'px';
	        div.style.top = cY + 'px';
	        div.style.width = colwidth+'px';
	        div.style.height = colheight+'px';
	        $(div).addClass('columnMarker');
	        document.getElementById('middlebody').appendChild(div);
	        $(div).dblclick(function(event) {
	            div.work_area_column_marker_removed = true;
	            helper.removeColumnMarkerById(idParam);
	        });
	        var div2 = document.createElement('div');
	        div2.style.position = 'relative';
	        div2.style.color = 'white';
	        div2.style.cursor = 'move';
	        div2.style.background = titleColor;
	        div2.style.opacity = '0.6';
	        div2.style.width = '100%';
	        div2.innerHTML = titleParam;
	        div.appendChild(div2);
	        var div3 = document.createElement('div');
	        div3.style.position = 'relative';
	        div3.style.width = '100%';
	        div3.style.height = '100%';
	        div3.style.background = bodyColor;
	        div3.style.opacity = '0.2';
	        div.appendChild(div3);
	        var el = $(div);
	        el.resizable();
	        el.draggable({ scroll: false });
	        helper.setTopMarker(parsingFilter);
	        
	        /* -- REPLACED WITH mousedown event
	        el.click(function( event ) {
	           	helper.setTopMarker(parsingFilter);
	            var params = {
	                "transaction": "work_area_column_marker_clicked",
	                "Id": idParam
	            };
	            helper.doCallBack(params);       
	        });
	        */
	        
	        el.mousedown(function(event) {
	        	helper.setTopMarker(parsingFilter);
	            var params = {
	                "transaction": "work_area_column_marker_clicked",
	                "Id": idParam
	            };
	            helper.doCallBack(params);       
	        	
	        });
	        
	        el.mouseup(function( event ) {
	            if (this.mouseUpTimout){
	                clearTimeout(this.mouseUpTimout);
	                this.mouseUpTimout = null;
	            }
	            //Delay the primary interval to avoid a server call while user active
	            helper.setUserIsActive();
	
	            this.mouseUpTimout = window.setTimeout(function(){
			        	
	                    if (div.work_area_column_marker_removed) return;
	                    var jdiv = $(div)
	                    var p = jdiv.position();
	                    var w = jdiv.outerWidth();
	                   	var h = jdiv.outerHeight();
	
	                    var object = new Object();
	                    object.top = Math.round(p.top);
	                    object.left = Math.round(p.left);
	                    object.width = Math.round(w);
	                    object.height = Math.round(h);
	
			            var params = {
	                        "transaction": "work_area_column_marker_changed",
	                        "Id": idParam,
	                        "object": object
			            };
			            helper.doCallBack(params);       
	            
	            },500);
	            
	        });
	        // Nofity of marker change event to trigger server call
	        // It may seem unneccesary to call the server at this point
	        // since the user will more than likely move and resize the marker after
	        // it is rendered in the work area. However; we will call the server
	        // to maintain consistancy.
	        if (! bypassEvent) {
	            this.fireMarkerChangedEvent(div, idParam);
	        }
	       
	    }
	    
	   	this.fireMarkerChangedEvent = function(div, idParam) {
	   		var helper = this;
	        var jdiv = $(div)
	        var p = jdiv.position();
	        var w = jdiv.outerWidth();
	        var h = jdiv.outerHeight();
	        var object = new Object();
	        object.top = Math.round(p.top);
	        object.left = Math.round(p.left);
	        object.width = Math.round(w);
	        object.height = Math.round(h);
	
	        var params = {
	            "transaction": "work_area_column_marker_changed",
	            "Id": idParam,
	            "object": object
	        };
	        helper.doCallBack(params);       
	    }
	      
	    /**
	 	 * Adds a column marker to the work area.
	 	**/
	    this.addColumnMarker = function(parsingFilter) {
	        if (!parsingFilter) return;
	        this.showColumnMarker(parsingFilter);
	    }
	    
	    /**
	 	 * Destroys all existing column markers
	 	**/
	    this.removeAllColumnMarkers = function() {
	        $(".columnMarker").remove();
	    }
	    
	    /**
	  	 * Destroys existing column markers for the filters param
	  	**/
	    this.removeColumnMarkers = function(filters) {
	        if (!filters || filters.length == 0) return;
	        for (var i = 0, len = filters.length; i < len; i++) {
	         	this.removeColumnMarkerById(filters[i].Id, true);
	        }
	    }
	    
	    /**
	  	 * Destroys existing column marker based on filter Id.
	  	**/
	    this.removeColumnMarker = function(parsingFilter) {
	        if (!parsingFilter) return;
	        this.removeColumnMarkerById(parsingFilter.Id);
	    }
	    
	    this.removeColumnMarkerById = function(Id, bypassEvent) {
	    	var helper = this;
	        if (!Id) return;
	        var el = document.getElementById(Id);
	        if (el != null){
	            el.remove();
	   	        var params = {
	                    "transaction": "work_area_column_marker_removed",
	                    "Id": Id
		        };
		        helper.doCallBack(params);       
	        }
	    }
	    
	    this.isColumnMarker = function(parsingFilter) {
	        if (!parsingFilter) return;
	        var el = document.getElementById(parsingFilter.Id);
	        return (el != null);
	    }
	    
	    this.setTopMarker = function(parsingFilter) {
	        if (!parsingFilter) return;
	        $(".columnMarker").css('z-index', 10);
	        $("#"+parsingFilter.Id).css('z-index', 11);
	        this.setMarkerSelected(parsingFilter.Id)
	    }
	    
	    this.getDefaultMarkerTitleColor = function(filterType) {
	        var titleColor = (!filterType || filterType == 'marker') ? '#4c8c64' : '#9f976e';
	        //var titleColor = filterType == 'marker' ? 'darkgreen' : 'darkorange';
	        return titleColor;
	    }
	    
	    this.getDefaultMarkerBodyColor = function(filterType) {
	        var bodyColor = (!filterType || filterType == 'marker') ? '#4bc076' : '#e6d478';
	        //var bodyColor = (!filterType || filterType == 'marker') ? 'green' : 'orange';
	        return bodyColor;
	    }   
	    
	    this.setMarkerSelected = function(Id) {
	      $(".columnMarker").css({"border-color": "", 
	             "border-width":"", 
	             "border-style":""});  
	        if (Id) {
	            $("#"+Id).css({"border-color": "blue", 
	                                "border-width":"1px", 
	                                "border-style":"solid"});  
	            //var el = document.getElementById(Id);
	            //if (el != null) el.scrollIntoView();
	        }
	    }
	    
	   
	  	this.fireMarkerChangedEvent = function(div, idParam) {
	        var jdiv = $(div)
	        var p = jdiv.position();
	        var w = jdiv.outerWidth();
	        var h = jdiv.outerHeight();
	        var object = new Object();
	        object.top = Math.round(p.top);
	        object.left = Math.round(p.left);
	        object.width = Math.round(w);
	        object.height = Math.round(h);
	        var params = {
	            "transaction": "work_area_column_marker_changed",
	            "Id": idParam,
	            "object": object
		  	};
		   	this.doCallBack(params);       
	    }
	    
	    /**
	  	 * Retrieves a parsing filter from the filters list
	     **/
	    this.getParsingFilterById = function(Id) {
	        return this.getFilterById(Id);
	    }
	    
	    this.getFilterById = function(Id) {
	        var filtersList = this.getFiltersArray();
	        for (var i = 0, len = filtersList.length; i < len; i++) {
	            if (Id == filtersList[i].Id) {
	                return filtersList[i];
	            }
	        }
	    }
	    
	    this.getFiltersArray = function() {
	        var arr = new Array();
	        var filtersList = this.parsingFilters;
	        if (filtersList) {
	            if (filtersList.length) {
	                return filtersList;
	            }
	            arr.push(filtersList);
	        }
	        return arr;
	    }
	    
	    this.getFiltersMapById = function() {
	        var map = {}
	        var filtersList = this.parsingFilters;
	        for (var i = 0, len = filtersList.length; i < len; i++) {
	            map[filtersList[i].Id] = filtersList[i];
	        }
	        return map;
	    }
	    
	    this.getClonedFilters = function() {
	    	if (this.parsingFilters == null) return null;
	    	return JSON.parse(JSON.stringify(this.parsingFilters));
	    }
	    
	    
	    this.isDocumentImported = function() {
	        return (this.documentKey && this.documentKey.length > 0);
	    }
	    
	    
	   	this.setPageNumber = function(n) {
	        if (! this.parsingFileInfo) return;
	        var c = this.parsingFileInfo.PageCount__c;
	        if (!c) return;
	        if (!n || isNaN(n) || n == 0) {
	            n = 1;
	        } 
	        if (n > c) n = c;
	        if (n < 1) n = 1;
	        this.parsingFileInfo.PageNumber__c = n;
	  		if (this.parsingFileInfo.pageNumber__c) {
	       		this.parsingFileInfo.pageNumber__c= n;
	       	}
	    }
	    
	  	this.pageUp = function() {
	        if (! this.parsingFileInfo) return;
	        var n = this.parsingFileInfo.PageNumber__c;
	        var c = this.parsingFileInfo.PageCount__c;
	        if (!c) return;
	        if (!n || isNaN(n) || n == 0) {
	            n = 1;
	        } else {
	            n--;
	        }
	        if (n > c) n = c;
	        if (n < 1) n = 1;
	        this.parsingFileInfo.PageNumber__c = n;
	  		if (this.parsingFileInfo.pageNumber__c) {
	       		this.parsingFileInfo.pageNumber__c= n;
	       	}
	    }
	    
	    this.pageDown = function() {
	        if (! this.parsingFileInfo) return;
	        var n = this.parsingFileInfo.PageNumber__c;
	        var c = this.parsingFileInfo.PageCount__c;
	        if (!c) return;
	        if (!n || isNaN(n) || n == 0) {
	            n = 1;
	        } else {
	            n++;
	        }
	        if (n > c) n = c;
	        if (n < 1) n = 1;
	        this.parsingFileInfo.PageNumber__c = n;
	 		if (this.parsingFileInfo.pageNumber__c) {
	       		this.parsingFileInfo.pageNumber__c= n;
	       	}
	    }
	    
	    this.getPageCount = function() {
	    	return this.parsingFileInfo.PageCount__c;
	    }
	    
	    
	    
	   	/**
	  	 * Makes sure the work area is in sync with the filters list
	  	**/
	    this.initEnvironment = function() {
	        var filtersMap = {};
	        var filtersList = this.parsingFilters;
	        
	        for(var i = 0, len = filtersList.length; i < len; i++) {
	            var p = filtersList[i];
	            filtersMap[p.Id] = p;
	            if (p.ColumnMarkerWidth__c > 0) {
	                if ( ! this.isColumnMarker(p) ) {
	                    this.showColumnMarker(p, true);                                       
	                }
	            }
	        };
	        // Remove any orphaned markers from the work area left possibly when filters were deleted.
	        /*
	      	var markers = this.getMarkerIds();
	       	markers.forEach(function(Id, index, arr){
	           this.removeColumnMarkerById(Id);
	       	}, this);
	  		*/
	        // Clear the currently selected filter if it was removed.
	        /*
	        var currentFilterId = this.getCurrentFilterId();
	        if (currentFilterId) {
	            if ( ! filtersMap[currentFilterId] ) {
	                this.clearCurrentFilterId();
	            }
	        };
	        */
	    }
	    
	    this.getMarkerIds = function() {
	        var markersArray = new Array();
	        var markers = $(".columnMarker");
	        if (markers) {
	            var i = 0;
	            $.each(markers, function( key, value ) {
	                var id = $(value).attr("id");
	                markersArray.push(id);
	            });
	        }
	        return markersArray;
	    }
	    
	    this.getMarkersMap = function() {
	        var markersMap = {};
	        var markers = $(".columnMarker");
	        if (markers) {
	            $.each(markers, function( key, value ) {
	                var id = $(value).attr("id");
	                markersMap[id] = value;
	            });
	        }
	        return markersMap;
	    }
	    
	   	this.reorderFilters = function(Ids) {
	        if (Ids == null || Ids.length == 0) return;
	        var IdsHash = {};
	        for (var i = 0, len = Ids.length; i < len; i++) {
	        	var Id = Ids[i];
	            IdsHash[Id] = i+1;
	        }
	        for (var i = 0, len = this.parsingFilters.length; i < len; i++) {
	            var fId = this.parsingFilters[i].Id;
	            var v = IdsHash[fId];
	            if (v && v > 0) {
	                this.parsingFilters[i].Order__c = v;
	            } else {
	                this.parsingFilters[i].Order__c = null;
	            }
	        }
	    }
	    
	    
	  	this.removeMetaItem = function(key) {
	       this.setMetaItem(key);
	    }
	    
	    this.setMetaItem = function(key, value) {
	        var p = this.parsingTemplate;
	        if (! p || ! key) return;
	        var metaObject = this.getMetaDataObject();    
	        if (! value) {
	             delete metaObject[key];
	        } else {
	            metaObject[key] = value;
	        }
	  		metaObject["lastupdated"] = new Date();
	        p.MetaData__c = JSON.stringify(metaObject);
	    }
	    
	    this.getMetaItem = function(key) {
	        var p = this.parsingTemplate;
	        if (! p || ! key) return;
	        var metaObject = this.getMetaDataObject();     
			return metaObject[key];
	    }
	    
	    this.getMetaDataObject = function() {
	        var p = this.parsingTemplate;
	        if (! p ) return;
	        var metaObject = (p.MetaData__c) ? JSON.parse(p.MetaData__c) : new Object();        
			return metaObject;
	    }
	    
	   	this.handleOnMouseUpGrabber1 = function() {
	        var fld = $(".grabber-input1");
	        this.handleOnMouseUp(fld);
	    }
	    
	    this.handleMaskButton = function(id, grabber) {
	        var src = event.srcElement;
	        if (src && src.id) {
	            var fld = $(".grabber-input1");
	            var ch = src.id.substring(0,1);
	        	this.keyConversionHandler(fld, ch, 1);
	        }
	    }
	
	    this.handleOnMouseUp = function(fld) {
	        var j = $(fld);
	        this.selStart = j[0].selectionStart;
	    }
	
	   	this.setKeyConversion = function(grabber) {
	        var helper = this;
	        if ( ! helper.selStart) helper.selStart = 0;
	        $('.grabber-input' + grabberIndex).on('keyup', function (e) {
	            helper.selStart = this.selectionStart;           
	            if (e.ctrlKey == false) return;
	            var ch = String.fromCharCode(e.keyCode).toUpperCase();
	            helper.keyConversionHandler(this, ch, grabber);
	            return false;
	        });
	    } 
	    
	    this.keyConversionHandler = function(fld, ch, grabber) {   
	        var ch = ch.toUpperCase();
	        if (ch.startsWith('A')) { newCh = String.fromCharCode(195);} else
	            if (ch.startsWith('B')) { newCh = String.fromCharCode(223);} else
	                if (ch.startsWith('N')) { newCh = String.fromCharCode(209);} else
	                    if (ch.startsWith('O')) { newCh = String.fromCharCode(248);} else
	                        return false;
	        
	        var j = jQuery(fld);
	        var val = j.val();
	        var s = this.selStart;
	        val = val.slice(0, s) + newCh + val.slice(s, val.length);
	        
	        grabber = val;
	        j.val(val);
	        
	        j[0].selectionStart = s+1;
	        j[0].selectionEnd = s+1;
	        
	        this.selStart = s+1;
	        
	       // component.set("v.fieldChangedSwitch", true);
	        
	    }    
  }
  
  /**
   * Static Methods
   */
	ParserCore.getDatePatterns = function() {
	 	var items = [
	       	{Name: "M/dd/yyyy", 			Description: '8/31/2018'},
	        {Name: "M/yy", 					Description: '8/18'},
	        {Name: "M/dd/yy", 				Description: '8/14/18'},
	        {Name: "MM/dd/yy", 				Description: '08/14/18'},
	        {Name: "dd-MMM", 				Description: '14-Aug'},
	        {Name: "dd-MMM-yy", 			Description: '14-Aug-18'},
	        {Name: "MMM-yy", 				Description: 'Aug-18'},
	        {Name: "MMMMM-yy", 				Description: 'August-18'},
	        {Name: "MMMMM dd, yyyy", 		Description: 'August 14, 2018'},
	        {Name: "M/dd/yyyy h:mm a", 		Description: '8/31/2018 1:30PM'},
	        {Name: "M/dd/yyyy H:mm", 		Description: '8/31/2018 13:30'},
	        {Name: "yyyy-MM-dd hh:mm:ss", 	Description: '2018-12-31 10:14:59'},
	        {Name: "yyyy-MM-dd'T'hh:mm:ss", Description: '2018-12-31T10:14:59'} 
	    ];
	
	    var items2 = new Array();
	    for (var i = 0, len = items.length; i < len; i++) {
	    	var o = new Object();
	    	o.Name = items[i].Name;
	    	var ds = 
	    	o.Description = items[i].Name + ' [e.g. ' + items[i].Description + ']';
	    	items2.push(o);
	    }
	    return items2;
	
	}
  
  
  