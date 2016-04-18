/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * Convert number of bytes into human readable format from:
 * http://codeaid.net/javascript/convert-size-in-bytes-to-human-readable-format-(javascript)
 * 
 * @param integer
 *            bytes Number of bytes to convert
 * @param integer
 *            precision Number of digits after the decimal separator
 * @return string
 */
function bytesToSize(bytes, precision) {
	var kilobyte = 1024;
	var megabyte = kilobyte * 1024;
	var gigabyte = megabyte * 1024;
	var terabyte = gigabyte * 1024;

	if ((bytes >= 0) && (bytes < kilobyte)) {
		return bytes + ' B';

	} else if ((bytes >= kilobyte) && (bytes < megabyte)) {
		return (bytes / kilobyte).toFixed(precision) + ' KB';

	} else if ((bytes >= megabyte) && (bytes < gigabyte)) {
		return (bytes / megabyte).toFixed(precision) + ' MB';

	} else if ((bytes >= gigabyte) && (bytes < terabyte)) {
		return (bytes / gigabyte).toFixed(precision) + ' GB';

	} else if (bytes >= terabyte) {
		return (bytes / terabyte).toFixed(precision) + ' TB';

	} else {
		return bytes + ' B';
	}
}

/**
 * Returns an array with two entries related to the current file pointer:
 * [offset, size]. If the pointer belongs to a rolled log, the values of the
 * current access log file are returned.
 * 
 * @param pointer
 *            the log pointer of a rolled or usual file log
 */
function getFilePointerPos(pointer) {
	if (typeof pointer == "undefined" || pointer == null) {
		return [ 0, 0 ];
	} else if (typeof pointer.u != "undefined"
			&& typeof pointer.u.o != "undefined") {
		return [ pointer.u.o, pointer.u.s ];
	} else if (typeof pointer.o != "undefined") {
		return [ pointer.o, pointer.s ];
	} else {
		return [ 0, 0 ];
	}
}

function LogPosition(name, disabled, active, isRolling, templatePointer,
		changeListener) {
	this.name = name;
	this.disabled = disabled;
	this.active = active;
	this.isRolling = isRolling;
	this.templatePointer = templatePointer;
	this.changeListener = changeListener;
	this.listenerMuted = false;

	var lp = this;
	this.currentFile = {
		fileSize : 0,
		filePos : 0,
		reset : function(filePos, fileSize) {
			this.filePos = filePos;
			this.fileSize = fileSize;
			if (!lp.disabled) {
				lp.currentSlider.max = this.fileSize - 1;
			}
			this.update();
		},
		setPos : function(filePos) {
			this.filePos = filePos;
			this.update();
		},
		update : function() {
			if (!lp.disabled) {
				lp.currentSlider.setValue(this.filePos);
			}
			if (lp.disabled) {
				var w = 0;
				if (this.fileSize > 0) {
					w = (this.filePos + 1) / this.fileSize * 100;
				}
				$('#' + lp.name + 'current .progress .bar').css("width",
						w + "%");
			}
			var msg = "";
			if (this.filePos == 0) {
				msg = "Start";
			} else if (this.filePos + 1 >= this.fileSize) {
				msg = "End";
			} else {
				msg = bytesToSize(this.filePos, 2);
			}
			$('#' + lp.name + 'current .info').html(
					msg + " of " + bytesToSize(this.fileSize, 2));
		}
	};
};

LogPosition.prototype.getCurrentPointer = function() {
	var pointer = this.templatePointer;
	if (typeof pointer != "undefined") {
        	var fp = pointer;
        	if (this.isRolling) {
        		var partSel = $('#' + this.name + 'part select');
        		pointer.p = partSel.val();
        		fp = {
        			s : $(partSel[0].options[partSel[0].selectedIndex]).data("size")
        		};
        		pointer.u = fp;
        	} else {
        	    fp.s = this.currentFile.fileSize;
        	}
        	fp.o = this.currentSlider.getValue();
	}
	return pointer;
};

LogPosition.prototype.initLogPartOption = function(sel, bytePos) {
	var i = sel.options.length - 1;
	if ($.isNumeric(bytePos)) {
		// Byte position
		for (; i > 0 && bytePos >= 0; i--) {
			var size = $(sel.options[i]).data("size");
			if (bytePos - size < 0) {
				break;
			}
			bytePos -= size;
		}
	} else if (typeof bytePos != "undefined" && bytePos != null) {
		for (i = 0; i < sel.options.length - 1; i++) {
			if (sel.options[i].value == bytePos.p) {
				break;
			}
		}
		if (bytePos.u && bytePos.u.s) {
			$(sel.options[i]).data("size", bytePos.u.s);
		}
	}
	this.currentFile.reset($.isNumeric(bytePos) ? bytePos
			: getFilePointerPos(bytePos)[0], $(sel.options[i]).data("size"));
	sel.selectedIndex = i;
	if (!this.disabled) {
		this.partSlider.setValue(sel.options.length - 1 - sel.selectedIndex);
	}
	return i;
};

LogPosition.prototype.initLogPart = function(currentBytePos) {
	if (!this.disabled) {
		var logObj = this;
		var sel = $('#' + logObj.name + 'part select')[0];
		this.partSlider = $('#' + this.name + 'part input.part-slider')
				.slider(
						{
							formater : function(value) {
								var opts = $('#' + logObj.name + 'part select')[0].options;
								return opts[opts.length - value - 1].label;
							},
							sliderClass : "slider progress"
									+ (logObj.active ? " progress-striped active"
											: ""),
							sliderTrackClass : "none",
							sliderSelectionClass : "slider-selection bar",
							value : 0
						}).on('slide', function(e) {
					var v = logObj.partSlider.getValue();
					this.value = v;
					sel.selectedIndex = sel.options.length - v - 1;
					try {
						logObj.listenerMuted = true;
						// $(sel).change();
					} finally {
						logObj.listenerMuted = false;
					}
				}).on('slideStop', function(e) {
					$(sel).change();
					// logObj.fireChangeListener();
				}).data('slider');
	}
	this.changeLogPartPosition(currentBytePos);
};

LogPosition.prototype.changeLogPartPosition = function(currentBytePos) {
	var logPartSel = $('#' + this.name + 'part select')[0];
	this.initLogPartOption(logPartSel, currentBytePos);
	var logPartOption = logPartSel.options.length - logPartSel.selectedIndex
			- 1;
	if (this.disabled) {
		$('#' + this.name + 'part .progress .bar').css("width",
				((logPartOption + 1) / logPartSel.options.length * 100) + "%");
	}
};

LogPosition.prototype.updateAfterRollingPartChange = function() {
	var logPartSel = $('#' + this.name + 'part select')[0];
	var selectedIndex = logPartSel.selectedIndex;
	this.partSlider.setValue(logPartSel.options.length - 1 - selectedIndex);
	this.currentFile.reset(0, $(logPartSel.options[selectedIndex]).data('size'));
	this.fireChangeListener();
};

LogPosition.prototype.fireChangeListener = function() {
	if (typeof this.changeListener != "undefined" && !this.listenerMuted) {
		this.changeListener.call(this, this.getCurrentPointer());
	}
};

LogPosition.prototype.init = function(currentBytePos, size) {
	if (!this.disabled) {
		var logObj = this;
		this.currentSlider = $(
				'#' + logObj.name + 'current input.filepos-slider')
				.slider(
						{
							formater : function(value) {
								if (value == 0) {
									return 'Start';
								} else if (value >= this.max) {
									return 'End';
								} else {
									return bytesToSize(value, 1);
								}
							},
							sliderClass : "slider progress"
									+ (logObj.active ? " progress-striped active"
											: ""),
							sliderTrackClass : "none",
							sliderSelectionClass : "slider-selection bar",
							value : logObj.currentFile.filePos,
							min : 0,
							max : logObj.currentFile.fileSize - 1,
							tooltipPlacement : logObj.isRolling ? "bottom"
									: "top"
						}).on('slide', function(e) {
					var v = logObj.currentSlider.getValue();
					logObj.currentFile.setPos(v);
				}).on('slideStop', function(e) {
					logObj.fireChangeListener();
				}).data('slider');
	}
	if (this.isRolling) {
		this.initLogPart(currentBytePos);
	} else {
		if ($.isNumeric(currentBytePos)) {
			this.currentFile.reset(currentBytePos, size?size:currentBytePos);
		} else {
			var p;
			if (currentBytePos) {
				p = getFilePointerPos(currentBytePos);
			} else {
				p = getFilePointerPos(this.templatePointer);
				p[0] = 0;
			}
			this.currentFile.reset(p[0], size?size:p[1]);
		}
	}
	this.currentFile.update();
};

LogPosition.prototype.changePosition = function(currentBytePos) {
    	if (currentBytePos && !this.templatePointer) {
    	    this.templatePointer = currentBytePos;
    	}
	if (this.isRolling) {
		this.changeLogPartPosition(currentBytePos);
		this.currentFile.update();
	} else {
		var p = getFilePointerPos(currentBytePos);
		this.currentFile.reset(p[0], p[1]);
	}
};

LogPosition.prototype.resetToStart = function() {
	if (this.isRolling) {
		this.changeLogPartPosition(0);
	} else {
		this.currentFile.setPos(0);
	}
	this.fireChangeListener();
};

LogPosition.prototype.resetToEnd = function() {
	if (this.isRolling) {
		var logPartSel = $('#' + this.name + 'part select')[0];
		logPartSel.selectedIndex = 0;
		var size = $(logPartSel.options[0]).data('size');
		this.currentFile.reset(size, size);
		this.partSlider.setValue(this.partSlider.max);
	} else {
		this.currentFile.setPos(this.currentFile.fileSize);
	}
	this.fireChangeListener();
};

(function() {

	function entriesHead(fieldsTypes, enabledViewerFields, cellsBeforeCallback, cellsAfterCallback) {
		var html = '<tr class="entries capitalized">';
		if (cellsBeforeCallback) {
			html += cellsBeforeCallback(fieldsTypes);
		}
		var fields2render = getFieldsToRender(fieldsTypes, enabledViewerFields);

		for (var z=0;z<fields2render.length;z++) {
			var name = fields2render[z];
			html += '<th>' + name + '</th>';
		}
		if (cellsAfterCallback) {
			html += cellsAfterCallback(fieldsTypes);
		}
		html += "</tr>";
		return html;
	};
	
	function getFieldsToRender(fieldsTypes, enabledViewerFields) {
		var fields = [];
		if (angular.isArray(enabledViewerFields) && enabledViewerFields.length > 0) {
			for(var i = 0; i < enabledViewerFields.length;i++) {
				var e = enabledViewerFields[i];
				if (e.enabled) {
					fields.push(e.key);
				}
			}
			if (fields.length > 0) {
				return fields;
			}
		}
		for (var name in fieldsTypes) {
			if ($.LogSniffer.entriesIgnoreFields[name]) {
				continue;
			}
			fields.push(name);
		}
		return fields;
	};

	function entriesRows(fieldsTypes, enabledViewerFields, entries, cellsBeforeCallback,
			cellsAfterCallback) {
		var html = "";
		
		var fields2render = getFieldsToRender(fieldsTypes, enabledViewerFields);
		var fLength = fields2render.length;

		for (var i = 0; i < entries.length; i++) {
			var e = entries[i];
			if (fLength > 0) {
				html += '<tr class="severity '
						+ getSeverityClass(e.lf_severity) + '" sof=\''
						+ e.lf_startOffset.sof + '\' start=\''
						+ JSON.stringify(e.lf_startOffset.json) + '\' eof=\'' + e.lf_endOffset.eof
						+ '\' end=\'' + JSON.stringify(e.lf_endOffset.json) + '\' onclick="$(this).toggleClass(\'selected\')">';
				if (cellsBeforeCallback) {
					html += cellsBeforeCallback(fieldsTypes, e);
				}
				for (var z=0;z<fields2render.length;z++) {
					var name = fields2render[z];
					html += '<td class="text '
							+ fieldsTypes[name]
							+ '">';
					if (e[name] == null && e.lf_unformatted) {
						html += '- parsing error -';
					} else {
						html += (e[name] != null ? printFieldValue(
									fieldsTypes[name], e[name]) : '');
					}
					html += '</td>';
				}
			} else {
				html += '<tr sof="' + e.lf_startOffset.sof + '" start=\''
						+ JSON.stringify(e.lf_startOffset.json) + '\' eof="' + e.lf_endOffset.eof
						+ '" end=\'' + JSON.stringify(e.lf_endOffset.json) + '\'>';
				if (cellsBeforeCallback) {
					html += cellsBeforeCallback(fieldsTypes, e);
				}
				html += '<td colspan="' + fLength + '" class="text">'
						+ e.lf_raw + '</td>';
			}
			if (cellsAfterCallback) {
				html += cellsAfterCallback(fieldsTypes, e);
			}
			html += "</tr>";
		}
		return html;
	}
	;

	function getSeverityClass(severity) {
		if (typeof severity != "undefined" && severity && typeof severity.c != "undefined") {
			return "sc-" + severity.c;
		} else {
			return "";
		}
	}

	function printFieldValue(fieldType, fieldValue) {
		if (typeof fieldValue!="undefined") {
			var html="";
			switch (fieldType) {
			case "SEVERITY":
				html = '<span class="label label-default severity sc-'+fieldValue.c+'">'+fieldValue.n+'</span>';
				break;
			case "DATE":
				html = LogSniffer.ng.dateFilter(fieldValue, 'medium');
				break;
			case "OBJECT":
				html = JSON.stringify(fieldValue);
				break;
			default:
				html = $("<div>").text(fieldValue).html();
			}
			return '<span class="'+fieldType+'">'+html+'</span>';
		} else {
			return null;
		}
	}

	$.LogSniffer = {
		entriesIgnoreFields : {
			"lf_raw" : true,
			"lf_timestamp" : true,
			"lf_severity" : true,
			"lf_startOffset": true,
			"lf_endOffset": true
		},
		entriesHead : entriesHead,
		entriesRows : entriesRows,
		printFieldValue : printFieldValue
	};
})();

var LogSniffer = LogSniffer || $.LogSniffer;
LogSniffer.ng = {};
LogSniffer.config = {};
LogSniffer.DataTableBinder = function(rowsPath, cols) {
	var index = function(obj, i) {
		return obj[i];
	};
	var pathToObj = function(path, obj) {
		if (!path) {
			return obj;
		}
		return path.split('.').reduce(index, obj);
	};

	this.rowsPath = rowsPath;
	this.cols = $.extend([], cols);

	this.rows = function(rowsPath) {
		this.rowsPath = rowsPath;
		return this;
	};

	this.col = function(colPath, colOptions) {
		this.cols.push({
			path : colPath,
			options : colOptions
		});
		return this;
	};

	this.bind = function(dataTable) {
		if (!dataTable) {
			dataTable = new google.visualization.DataTable();
		}
		for (var i = 0; i < this.cols.length; i++) {
			var colDef = this.cols[i];
			dataTable.addColumn(colDef.options);
		}
		var binder = this;
		return function(response) {
			$
					.each(
							pathToObj(binder.rowsPath, response),
							function(i, rowSource) {
								var row = [];
								for (var i = 0; i < binder.cols.length; i++) {
									var colDef = binder.cols[i];
									var colValue = pathToObj(colDef.path,
											rowSource);
									colValue = typeof colValue != "undefined" ? colValue
											: null;
									if (colValue != null) {
										if ((colDef.options.type == "date" || colDef.options.type == "datetime")
												&& !(colValue instanceof Date)) {
											colValue = new Date(colValue);
										} else if (colDef.options.type == "timeofday") {
											var d = new Date(colValue);
											colValue = [d.getHours(), d.getMinutes(), d.getSeconds(), d.getMilliseconds()];
										}
									}
									row[i] = colValue;
								}
								dataTable.addRow(row);
							});
			return dataTable;
		};
	};
};

LogSniffer.objSize = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

LogSniffer.moveUp = function(array, index) {
    var value = array[index];     
    var newPos = index - 1;
    if(newPos < 0) {
        newPos = 0;
    }
    array.splice(index, 1);
    array.splice(newPos, 0, value);
};

LogSniffer.moveDown = function(array, index) {
    var value = array[index];     
    var newPos = index + 1;
	if(newPos >= array.length) {
	    newPos = array.length;
	}
	array.splice(index, 1);
	array.splice(newPos, 0, value);
};


LogSniffer.DataSourceBuilder = function(savedData) {
	$.extend(this, {
		builders : [],
		joinMethod : "full",
		joinKeys : [ [ 0, 0 ] ]
	}, savedData);
	for (var i = 0; i < this.builders.length; i++) {
		var bData = this.builders[i];
		this.builders[i] = new (LogSniffer.DataSourceBuilder[bData._type])(
				bData);
	}
	
	this.build = function(params) {
		if (this.builders.length>0) {
			var tables = [];
			for (var i = 0; i < this.builders.length; i++) {
				var builder = this.builders[i];
				tables[i] = builder.build(params);
			}
			return LogSniffer.DataSourceBuilder.joinDataTables(tables, this.joinMethod, this.joinKeys);
		} else {
			console.log("No builders defined, return empty data table");
			return new google.visualization.DataTable();
		}
	};
};

LogSniffer.DataSourceBuilder.ElasticRequestBuilder = function(savedData) {
	$.extend(this, {
		_type : "ElasticRequestBuilder",
		request: {
			facets: {},
			aggs: {},
			size: 0
		},
		requestParts: [],
		joinMethod : "full",
		joinKeys : [ [ 0, 0 ] ]
	}, savedData);

	for (var i = 0; i < this.requestParts.length; i++) {
		var data = this.requestParts[i];
		this.requestParts[i] = new (LogSniffer.DataSourceBuilder.ElasticRequestBuilder[data._type])(
				data);
	}
	this.build = function(params) {
		var request = angular.copy(this.request);
		var parts = this.requestParts;
		$.each(parts, function(i, part) {
			part.prepareRequest(i, request);
		});
		var requestStr = JSON.stringify(request);
		console.log("Executing datSource events search: " + requestStr);
		var tables = [];
		$.ajax($.extend({
			type : "POST",
			contentType : "application/json",
			async : false,
			data : requestStr
		}, params.ajax)).success(
				function(response) {
					$.each(parts, function(i, part) {
						var table = part.bindResponse(i, response);
						if (table) {
							tables.push(table);
						}
					});
				});
		return LogSniffer.DataSourceBuilder.joinDataTables(tables, this.joinMethod, this.joinKeys);
	};
};

LogSniffer.DataSourceBuilder.ElasticRequestBuilder.DateHistogramFacet = function(savedData) {
	$.extend(true, this, {
		_type : "DateHistogramFacet",
		name : "",
		request : {
			date_histogram: {},
			facet_filter: {
			}
		},
		cols : [
		        {}, {}
		]
	}, savedData);

	this.prepareRequest = function(i, request) {
		request.facets["f"+i] = this.request;
	};
	
	this.bindResponse = function(i, response) {
		var table = new google.visualization.DataTable();
		var binder = new LogSniffer.DataTableBinder(null,
				this.cols);
		binder.bind(table)(response.facets["f"+i].entries);
		return table;
	};
};

LogSniffer.DataSourceBuilder.ElasticRequestBuilder.StatsAgg = function(savedData) {
	$.extend(true, this, {
		_type : "StatsAgg",
		name : "",
		request : {
			filter: {
				match_all : { }
			},
			aggs: {
				statistics: {
					stats: {}
				}
			}
		},
		cols : []
	}, savedData);

	this.prepareRequest = function(i, request) {
		request.aggs["a"+i] = this.request;
	};
	
	this.bindResponse = function(i, response) {
		var table = new google.visualization.DataTable();
		var binder = new LogSniffer.DataTableBinder(null,
				this.cols);
		binder.bind(table)(response.aggregations["a"+i].statistics);
		return table;
	};
};

LogSniffer.DataSourceBuilder.ElasticRequestBuilder.TermsAgg = function(savedData) {
	$.extend(true, this, {
		_type : "TermsAgg",
		name : "",
		request : {
			filter: {
				match_all : { }
			},
			aggs: {
				terms: {
					terms: {}
				}
			}
		},
		cols : [{
			options: {
				label: "Term",
				type: "string"				
			},
			path: "key"
		}, {
			options: {
				label: "Count",
				type: "number"				
			},
			path: "doc_count"
		}]
	}, savedData);

	this.prepareRequest = function(i, request) {
		request.aggs["a"+i] = this.request;
	};
	
	this.bindResponse = function(i, response) {
		var table = new google.visualization.DataTable();
		var binder = new LogSniffer.DataTableBinder(null,
				this.cols);
		binder.bind(table)(response.aggregations["a"+i].terms.buckets);
		return table;
	};
};

LogSniffer.DataSourceBuilder.joinDataTables = function(tables, joinMethod, joinKeys) {
	var leftTable = tables[0];
	for (var i = 1; i < tables.length; i++) {
		var rightTable = tables[i];
		var leftTableCols = [];
		for (var z = 0; z < leftTable.getNumberOfColumns(); z++) {
			for (var u = 0; u < joinKeys.length; u++) {
				if (joinKeys[u][0] != z) {
					leftTableCols.push(z);
				}
			}
		}
		var rightTableCols = [];
		for (var z = 0; z < rightTable.getNumberOfColumns(); z++) {
			for (var u = 0; u < joinKeys.length; u++) {
				if (joinKeys[u][1] != z) {
					rightTableCols.push(z);
				}
			}
		}
		leftTable = google.visualization.data.join(leftTable, rightTable,
				joinMethod, joinKeys, leftTableCols,
				rightTableCols);
	}
	return leftTable;
};

LogSniffer.objToFormFields = function(obj, path) {
	var f = function(fields, path, value) {
		if (angular.isArray(value)) {
			for(var i=0;i<value.length;i++) {
				f(fields, path+"["+i+"]", value[i]);
			}
		} else if (angular.isObject(value)) {
			for(var i in value) {
				f(fields, (path?path+".":"")+i, value[i]);
			}
		} else {
			fields[path] = value;
		}
	};
	var fields={};
	f(fields, path, obj);
	return fields;
};

LogSniffer.ng.BeanWizardController = function($scope) {
	$scope.selectedWizard = null;
	$scope.templateLoading = false;
	
	var unwrapAndSetBean = function(beanWrapper, setType) {
		$scope.bean = beanWrapper.length > 0 ? beanWrapper[0]: {};
		var beanType = $scope.bean ? $scope.bean["@type"]: null;
		if (setType && beanType) {
			for(var z=0;z<$scope.wizards.length;z++) {
				var wizard=$scope.wizards[z];
				if (wizard.beanType == beanType) {
					$scope.selectedWizard = wizard;
					break;
				};
			};
		}
	};
	unwrapAndSetBean($scope.beanWrapper, true);

	$scope.$watch('bean', function(newValue, oldValue) {
		if ($scope.selectedWizard) {
			$scope.beanWrapper[0] = newValue;
		}
    });
	$scope.$watch('selectedWizard', function(newValue, oldValue) {
		if (newValue != oldValue && newValue) {
			unwrapAndSetBean(angular.copy([newValue.template]));
		}
		if (!newValue) {
			unwrapAndSetBean({});
		}
		if (newValue) {
			$scope.bean["@type"]=newValue.beanType;
			$scope.beanWrapper[0] = $scope.bean;
			$scope.templateLoading = true;
		}
    });
	
	$scope.templateLoaded = function() {
		$scope.templateLoading = false;
	};
};

LogSniffer.stripPrefix = function(source, prefix) {
	var target={};
	if (source) {
		for(var i in source) {
			if (i.indexOf(prefix)==0 && i.length>prefix.length) {
				target[i.substr(prefix.length)]=source[i];
			}
		}
	}
	return target;
};
LogSniffer.containsKey = function(source, keys, startsWith) {
	if (!source) {
		return false;
	}
	if (!angular.isArray(keys)) {
		keys = [keys];
	}
	for(var z=0;z < keys.length;z++) {
		var key = keys[z];
		for(var i in source) {
			if (i==key || startsWith && i.indexOf(key)==0) {
				return true;
			}
		}
	}
	return false;
};
LogSniffer.hasKeysExpectOf = function(source, keys, startsWith) {
	if (!source) {
		return false;
	}
	if (!angular.isArray(keys)) {
		keys = [keys];
	}
	var total=0, matching=0;
	for(var i in source) {
		total++;
		for(var z=0;z < keys.length;z++) {
			var key = keys[z];
			if (i==key || startsWith && i.indexOf(key)==0) {
				matching++;
				break;
			}
		}
	}
	return total > matching;
};

LogSniffer.getFieldType = function (fields, fieldName) {
	if (fields && fields["@types"] && fields["@types"][fieldName]) {
	    return fields["@types"][fieldName];
	} else {
	    return null;
	}
};

