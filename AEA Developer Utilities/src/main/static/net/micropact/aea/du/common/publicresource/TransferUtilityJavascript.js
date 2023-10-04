/* public-resource */

"use strict";

window.AeaTransferUtility = (function(DUC) {

	const E = React.createElement

	function makeTransfer(users, items, toUser, initialSelectedFromUsers, initialSelectedItems) {
		var state = {
			users: users,
			items: items,

			selectedFromUsers: {},
			selectedToUser: toUser === null ? "" : toUser,
			selectedItems: {},
			selectAllUsers: false,
			selectAllItems: false,

			userIdIndex: {},
			itemIdIndex: {},
			itemsByUserIdIndex: {},

			listeners: []
		}

		/* Begin state initialization functions */
		addIndices()

		jQuery(initialSelectedItems).each(function(_index, itemId) {
			selectItem(itemId)
		})

		jQuery(initialSelectedFromUsers).each(function(_index, userId) {
			selectFromUser(userId)
		})

		function addMultiIndex(index, key, value) {
			if (key) {
				if (index[key] === undefined) {
					/* Should probably also use a Map for this */
					index[key] = []
				}
				index[key].push(value)
			}
		}

		function addIndices() {
			var stateUsers = state.users
			var stateItems = state.items

			stateUsers.forEach(function(user) {
				state.userIdIndex[user.USER_ID] = user
			})

			stateItems.forEach(function(item) {
				state.itemIdIndex[item.ITEM_ID] = item
				addMultiIndex(state.itemsByUserIdIndex, item.USER_ID, item)
			})
		}

		function callListeners() {
			state.listeners.forEach(function(listener) {
				listener()
			})
		}

		function addListener(func) {
			state.listeners.push(func)
		}

		function removeListener(func) {
			for (var i = 0; i < state.listeners.length; i++) {
				if (state.listeners[i] == func) {
					state.listeners.splice(i, 1)
					return
				}
			}
			console.error("Listener not found.")
		}

		function getSelectedToUser() {
			return state.selectedToUser
		}

		function getUsersWithCreate() {
			return state.users.filter(function(user) {
				return user.HASCREATE == 1
			})
		}

		/* Begin State interaction functions */
		function getUser(userId) {
			return state.userIdIndex[userId]
		}

		function getItem(itemId) {
			return state.itemIdIndex[itemId]
		}

		function getNumberOfUserItems(userId) {
			var stateItems = state.itemsByUserIdIndex[userId]
			return stateItems ? stateItems.length : 0
		}

		function getUsersWithItems() {
			return jQuery(state.users).filter(function(_index, user) {
				return getNumberOfUserItems(user.USER_ID) > 0
			}).toArray()
		}

		function getSelectedFromUsersItems() {
			var arr = []
			for (var userId in state.selectedFromUsers) {
				arr = arr.concat(state.itemsByUserIdIndex[userId] || [])
			}

			return arr.sort(function(a, b) {
				return a.NAME < b.NAME ? -1 : a.NAME == b.NAME ? 0 : 1
			})
		}

		function selectFromUser(userId) {
			state.selectedFromUsers[userId] = getUser(userId)
			callListeners()
		}

		function deselectFromUser(userId) {
			delete state.selectedFromUsers[userId]
			callListeners()
		}

		function itemIsSelected(itemId) {
			return state.selectedItems[itemId] !== undefined
		}

		function fromUserSelected(userId) {
			return state.selectedFromUsers[userId] !== undefined
		}

		function selectItem(itemId) {
			state.selectedItems[itemId] = getItem(itemId)
			callListeners()
		}

		function deselectItem(itemId) {
			delete state.selectedItems[itemId]
			callListeners()
		}

		/*
		 * TODO: There is actually a bug in this implementation.
		 * If you select all, then select a user, everything is out of sync.
		 * No need to fix it until somebody notices.
		 */
		function toggleSelectAllItems() {
			var isSelected = state.selectAllItems

			if (isSelected) {
				state.selectedItems = {}
			} else {
				jQuery(getSelectedFromUsersItems()).each(function(_index, item) {
					selectItem(item.ITEM_ID)
				})
			}
			state.selectAllItems = !state.selectAllItems
			callListeners()
		}

		function toggleFromUser(userId) {
			var isSelected = fromUserSelected(userId)
			if (isSelected) {
				deselectFromUser(userId)
			} else {
				selectFromUser(userId)
			}

			state.selectAllItems = false

			callListeners()
		}

		function selectToUser(userId) {
			state.selectedToUser = userId
			callListeners()
		}

		return {
			getUsersWithCreate: getUsersWithCreate,
			getUser: getUser,
			getItem: getItem,
			getNumberOfUserItems: getNumberOfUserItems,
			getUsersWithItems: getUsersWithItems,
			getSelectedFromUsersItems: getSelectedFromUsersItems,
			itemIsSelected: itemIsSelected,
			fromUserSelected: fromUserSelected,
			selectItem: selectItem,
			deselectItem: deselectItem,
			toggleSelectAllItems: toggleSelectAllItems,
			toggleFromUser: toggleFromUser,
			selectToUser: selectToUser,
			getSelectedToUser: getSelectedToUser,

			addListener: addListener,
			removeListener: removeListener
		}
	}

	function Transferred(props) {
		return props.displayTransferred &&
			E(AeaCoreReactComponents.Successes, {
				successes: [props.transferTypePlural + " Successfully Transferred"]
			})
	}

	function TransferTo(props) {
		var options = props.transfer.getUsersWithCreate().map(function(user) {
			return E("option", {
				value: user.USER_ID,
				key: user.USER_ID
			},
				user.USERNAME)
		})

		return E("div", null,
			E("label", {
				className: "formItem"
			}, "Transfer To"),
			E("select", {
				name: "toUser",
				value: props.transfer.getSelectedToUser(),
				onChange: function(event) {
					props.transfer.selectToUser(event.target.value)
				}
			},
				E("option", {
					value: ""
				}),
				options))
	}

	function UsersWithItems(props) {
		var userItems = props.transfer.getUsersWithItems().map(function(user) {

			var toggleUser = function() {
				props.transfer.toggleFromUser(user.USER_ID)
			}

			return E("li", {
				key: user.USER_ID
			},
				E("input", {
					type: "checkbox",
					name: "fromUsers",
					value: user.USER_ID,
					checked: props.transfer.fromUserSelected(user.USER_ID),
					onChange: toggleUser
				}),
				E("label", {
					onClick: toggleUser
				},
					E("span", null, user.USERNAME),
					E("span", {
						className: "aea-core-count"
					}, props.transfer.getNumberOfUserItems(user.USER_ID))))
		})
		return E("div", null,
			E("label", {
				className: "formItem"
			}, "Users with " + props.transferTypePlural),
			E("ul", {
				className: "fromUsers formItem"
			},
				userItems))
	}

	function SelectAllButton(props) {
		return E("input", {
			type: "checkbox",
			checked: props.transfer.selectAllItems,
			onChange: props.transfer.toggleSelectAllItems
		})
	}

	function Items(props) {
		var hasBusinessKey = props.hasBusinessKey

		var rows = props.transfer.getSelectedFromUsersItems().map(function(item) {
			var isSelected = props.transfer.itemIsSelected(item.ITEM_ID)
			return E("tr", {
				key: item.ITEM_ID
			},
				E("td", null, E("input", {
					type: "checkbox",
					name: "items",
					value: item.ITEM_ID,
					checked: isSelected,
					onChange: function() {
						if (isSelected) {
							props.transfer.deselectItem(item.ITEM_ID)
						} else {
							props.transfer.selectItem(item.ITEM_ID)
						}
					}
				})),
				E("td", null, item.NAME),
				E("td", null, props.transfer.getUser(item.USER_ID).USERNAME),
				hasBusinessKey ? E("td", null, item.BUSINESS_KEY) : null)
		})

		return E("table", {
			className: "grid aea-core-grid"
		},
			E("thead", null, E("tr", null,
				E("th", null, E("label", null,
					E(SelectAllButton, {
						transfer: props.transfer
					}),
					"Select All/None")),
				E("th", null, props.transferType + " Name"),
				E("th", null, "Owner"),
				hasBusinessKey ? E("th", null, "Business Key") : null)),
			E("tbody", null, rows))
	}

	function Submit(props) {
		return E("input", {
			type: "submit",
			className: "formButton",
			value: "Transfer " + props.transferTypePlural
		})
	}

	function ItemMover(props) {
		const [state, setState] = React.useState({
			transfer: props.transfer,
			transferCallback: null
		})

		React.useEffect(function() {
			var callback = function() {
				setState(Object.assign({}, state, {
					transfer: state.transfer
				}))
			}

			setState(Object.assign({}, state, {
				transferCallback: callback
			}))
			state.transfer.addListener(callback)

			return function unmount() {
				state.transfer.removeListener(state.transferCallback)
			}
		}, [])


		var transferToProps = {
			transfer: props.transfer
		}

		return E("div", null,
			E(Transferred, {
				displayTransferred: props.displayTransferred,
				transferType: props.transferType,
				transferTypePlural: props.transferTypePlural
			}),
			E(DUC.Errors, {
				errors: props.errors
			}),
			E(AeaCoreReactComponents.Form, {
				csrfToken: props.csrfToken,
				method: "POST",
				action: props.submissionUrl
			},
				E("input", {
					type: "hidden",
					name: "action",
					value: "transfer"
				}),
				E(UsersWithItems, {
					transfer: props.transfer,
					transferType: props.transferType,
					transferTypePlural: props.transferTypePlural
				}),
				E(TransferTo, transferToProps),
				E(Items, {
					hasBusinessKey: props.hasBusinessKey,
					transfer: props.transfer,
					transferType: props.transferType,
					transferTypePlural: props.transferTypePlural
				}),
				E(TransferTo, transferToProps),
				E(Submit, {
					transfer: props.transfer,
					transferType: props.transferType,
					transferTypePlural: props.transferTypePlural
				})))
	}

	function ItemMoverApp(props) {
		return E(DUC.Application, {
			title: "Transfer " + props.transferType + " Ownership",
			instructions: ["This page can transfer ownership of " + props.transferTypePlural + " from one user to another.",
				"This can become important when a developer leaves or deployments need to be done to production.",
			"Currently if you delete a user, it deletes their " + props.transferTypePlural + " and this can be a problem."
			]
		},
			E(ItemMover, {
				csrfToken: props.csrfToken,
				hasBusinessKey: props.hasBusinessKey,
				transfer: props.transfer,
				displayTransferred: props.displayTransferred,
				errors: props.errors,
				submissionUrl: props.submissionUrl,
				transferType: props.transferType,
				transferTypePlural: props.transferTypePlural
			})
		)
	}

	return {
		makeTransfer: makeTransfer,
		ItemMoverApp: ItemMoverApp
	}
}(AeaCoreReactComponents))