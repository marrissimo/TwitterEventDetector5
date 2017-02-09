	
    		var startDate,
            endDate,
            updateStartDate = function() {
                startPicker.setStartRange(startDate);
                endPicker.setStartRange(startDate);
                endPicker.setMinDate(startDate);
            },
            updateEndDate = function() {
                startPicker.setEndRange(endDate);
                startPicker.setMaxDate(endDate);
                endPicker.setEndRange(endDate);
            },
            startPicker = new Pikaday({
                field: document.getElementById('start'),
                minDate: new Date(2015, 11, 21),
                maxDate: new Date(),
                onSelect: function() {
                    startDate = this.getDate();
                    updateStartDate();
                }
            }),
            endPicker = new Pikaday({
                field: document.getElementById('end'),
                minDate: new Date(2015, 11, 21),
                maxDate: new Date(),
                onSelect: function() {
                    endDate = this.getDate();
                    updateEndDate();
                }
            }),
            _startDate = startPicker.getDate(),
            _endDate = endPicker.getDate();

            if (_startDate) {
                startDate = _startDate;
                updateStartDate();
            }

            if (_endDate) {
                endDate = _endDate;
                updateEndDate();
            }
            function updateInput(ish){
        	    document.getElementById("end").value = ish;
        		
        	}
            
            
            
            var startVDate,
            endVDate,
            updateStartVDate = function() {
                startVPicker.setStartRange(startVDate);
                endVPicker.setStartRange(startVDate);
                endVPicker.setMinDate(startVDate);
            },
            updateEndVDate = function() {
                startVPicker.setEndRange(endVDate);
                startVPicker.setMaxDate(endVDate);
                endVPicker.setEndRange(endVDate);
            },
            startVPicker = new Pikaday({
                field: document.getElementById('startV'),
                minDate: new Date(2015, 11, 21),
                maxDate: new Date(),
                onSelect: function() {
                    startVDate = this.getDate();
                    updateStartVDate();
                }
            }),
            endVPicker = new Pikaday({
                field: document.getElementById('endV'),
                minDate: new Date(2015, 11, 21),
                maxDate: new Date(),
                onSelect: function() {
                    endVDate = this.getDate();
                    updateEndVDate();
                }
            }),
            _startVDate = startVPicker.getDate(),
            _endVDate = endVPicker.getDate();

            if (_startVDate) {
                startVDate = _startVDate;
                updateStartVDate();
            }

            if (_endVDate) {
                endVDate = _endDate;
                updateEndVDate();
            }
            function updateInput(ish){
        	    document.getElementById("endV").value = ish;
        		
        	}
            
            
            