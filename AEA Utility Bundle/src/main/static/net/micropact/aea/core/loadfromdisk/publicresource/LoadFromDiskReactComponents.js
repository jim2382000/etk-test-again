/* public-resource */

"use strict"

window.LoadFromDiskReactComponents = (function() {
	const E = React.createElement

	function DisableWhenLoadFromDisk(props) {
		const [isLoadFromDisk, setLoadFromDisk] = React.useState()

		React.useEffect(() => {
			EntellitrakOAuth.ajaxCurrentUser("net/micropact/aea/core/loadfromdisk/endpoint/LoadFromDiskEndpoint/getSystemConfig", {})
				.done(result => {
					setLoadFromDisk(result.isLoadFromDisk)
				})
		}, [])

		if (isLoadFromDisk === undefined) {
			return E(AeaCoreReactComponents.Loading)
		} else if (isLoadFromDisk) {
			return E("div", null, "This functionality is not available in Load From Disk environments")
		} else {
			return E(React.Fragment, null, props.children)
		}
	}

	return {
		DisableWhenLoadFromDisk
	}
}())