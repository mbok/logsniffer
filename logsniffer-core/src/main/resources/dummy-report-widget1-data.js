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
{
	chartType: "BarChart",
	options: {
        "title": "Severities statistic",
        "fill": 20,
    },
    formatters: {},
	dataBuilder: {
	      _type: "Facets",
	      facets: [
	            {
	            	_type: "TermsFacet",
	            	name: "Severities",
	            	request: {
	            		"terms" : {
	            			"field" : "entries.fields._severity.n"
	            		},
            			"facet_filter": {
                            "range": {
                                "occurrence": {
                                    "from": "2012-01-01",
                                    "to": "2012-12-31"
                                }
                            }
                        }	            		
	            	},
	            	cols: [
	            	     {
	            	    	 path: "term",
	            	    	 options: {
	            	    		 label: "Severity",
	            	    		 type: "string"
	            	    	 }
	            	     },
	            	     {
	            	    	 path: "count",
	            	    	 options: {
	            	    		 type: "number",
	            	    		 label: "Events count in 2012"
	            	    	}
	            	     }		            	     
	            	]
	            },
	            {
	            	_type: "TermsFacet",
	            	name: "Severities",
	            	request: {
	            		"terms" : {
	            			"field" : "entries.fields._severity.n"
	            		},
            			"facet_filter": {
                            "range": {
                                "occurrence": {
                                    "from": "2013-01-01"
                                }
                            }
                        }	            		
	            	},
	            	cols: [
	            	     {
	            	    	 path: "term",
	            	    	 options: {
	            	    		 label: "Severity",
	            	    		 type: "string"
	            	    	 }
	            	     },
	            	     {
	            	    	 path: "count",
	            	    	 options: {
	            	    		 type: "number",
	            	    		 label: "Events count in 2013"
	            	    	}
	            	     }		            	     
	            	]
	            }	            
	      ]
	}
}