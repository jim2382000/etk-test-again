/**
 *
 * SCI User Profile JS
 *
 * brosenburgh 04/01/2021
 **/

jQuery(function($) {

	$('#SciUserProfile_gitAccessToken').attr({type: 'password'});
	
	if ($('#mainContent').find('input[name=update]').val() === 'true') {
		$('#SciUserProfile_gitAccessToken').attr({disabled: 'disabled'});
	}

});
