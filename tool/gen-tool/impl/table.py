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
            "i18n": True,
            "key": "key",
            "unique": [
                ["key"]
            ]
        },
        "st_property": {
            "i18n": True,
            "key": "config_id",
            "unique": [
                ["config_id"]
            ]
        },
        "st_rank": {
            "i18n": True,
            "key": "config_id",
            "unique": [
                ["config_id"]
            ]
        },
        "st_reward": {
            "akey": "reward_id",
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