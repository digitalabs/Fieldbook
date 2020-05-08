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
			available: 50
		}, "PNMCLATE-22": {
			available: 100
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
			available: 71
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
			available: 50
		}, "CA2019-0342": {
			available: 150
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
			available: 50
		}, "perf-stock-2": {
			available: 150
		}, "perf-stock-3": {
			available: 150
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
				available: 50
			}, "perf-stock-2": {
				available: 150
			}, "perf-stock-3": {
				available: 150
			}
		}
	}
})) // test performance
