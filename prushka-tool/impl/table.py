class Table:
    tables = {
        "st_bitmap": {
            "key": "config_id",
            "exclude": ["file"],
            "unique": [
                ["config_id"]
            ]
        },
        "st_constant": {
            "key": "key",
            "unique": [
                ["key"]
            ]
        },
        "st_message": {
            "key": "key",
            "unique": [
                ["key"]
            ]
        },
        "st_property": {
            "key": "config_id",
            "unique": [
                ["config_id"]
            ]
        },
        "st_rank": {
            "key": "config_id",
            "unique": [
                ["config_id"]
            ]
        },
        "st_reward": {
            "key": "reward_id",
            "unique": [
                ["reward_id"]
            ]
        },
        "st_reward_pool": {
            "akey": "pool_id",
            "unique": [
                ["pool_id"]
            ]
        },
    }