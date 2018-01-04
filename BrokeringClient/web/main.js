/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function switchTab(header) {
    var parent = $(header).parent();
    var children = parent.children('[tab]');
    
    for(var i = 0; i < children.length; i++) {
        $(children[i]).removeClass('active');
        if($(children[i]).is('span')) {
            $(children[i]).css('display', 'none');
        }
    }
    $(header).addClass('active');
    var content = $(parent).children('span[tab="' + $(header).attr('tab') + '"]');
    content.css('display', '');
}

function hideAllColumns(columns) {
    for (var i = 0; i < columns.length; i++) {
        columns[i].style.display = 'none';
    }
}

function tableClick(row) {
    var tableRows = $('#searched > table > tbody > tr');
    for (var i = 0; i < tableRows.length; i++) {
        tableRows[i].style.backgroundColor = '';
        tableRows[i].style.color = '';
    }
    row.style.backgroundColor = 'purple';
    row.style.color = 'white';


    var columns = $('div[id^=column_');
    var clickIndex = row.rowIndex;
    hideAllColumns(columns);


    var col1 = $('div[id=column_1_' + (clickIndex - 1).toString() + ']');
    col1.css('display', '');

    var col2 = $('div[id=column_2_' + (clickIndex - 1).toString() + ']');
    col2.css('display', '');

//    .css('display', "");
//    $('div[id=column_2_' + clickIndex - 1 + ']').css('display', "");
}
