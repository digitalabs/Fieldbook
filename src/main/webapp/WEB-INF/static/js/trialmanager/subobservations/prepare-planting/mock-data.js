// TODO move to unit test.
mockUnits = {
	SEED_AMOUNT_g: {
		lotsNo: 0,
		groupTransactions: true,
		withdrawAll: false,
		amountPerPacket: 0
	}, SEED_COUNT: {
		lotsNo: 0,
		groupTransactions: true,
		withdrawAll: false,
		amountPerPacket: 0
	}
};
mockEntries = [{
	entryNo: 1,
	entryType: "test",
	gid: 11,
	designation: "pinto o negro",
	unit: "SEED_AMOUNT_g",
	numberOfPackets: 4,
	stock: {
		"PNMCLATE-21": {
			availableBalance: 50
		}, "PNMCLATE-22": {
			availableBalance: 100
		}
	}
}, {
	entryNo: 2,
	entryType: "test",
	gid: 11,
	designation: "pinto o negro",
	unit: "SEED_COUNT",
	numberOfPackets: 2,
	stock: {
		"MKC-0124": {
			availableBalance: 71
		}
	}
}, {
	entryNo: 3,
	entryType: "test",
	gid: 11,
	designation: "pinto o negro",
	unit: "SEED_COUNT",
	numberOfPackets: 5,
	stock: {
		"CA2019-0341": {
			availableBalance: 50
		}, "CA2019-0342": {
			availableBalance: 150
		}
	}
}, {
	entryNo: 4,
	entryType: "test",
	gid: 14,
	designation: "perf testing entry",
	unit: "SEED_AMOUNT_g",
	numberOfPackets: 4,
	stock: {
		"perf-stock-1": {
			availableBalance: 50
		}, "perf-stock-2": {
			availableBalance: 150
		}, "perf-stock-3": {
			availableBalance: 150
		}
	}
}].concat([...Array(996).keys()].map((i) => {
	return {
		entryNo: i+5,
		entryType: "test",
		gid: i+5,
		designation: "perf testing entry",
		unit: "SEED_AMOUNT_g",
		numberOfPackets: 6,
		stock: {
			"perf-stock-1": {
				availableBalance: 50
			}, "perf-stock-2": {
				availableBalance: 150
			}, "perf-stock-3": {
				availableBalance: 150
			}
		}
	}
})) // test performance
