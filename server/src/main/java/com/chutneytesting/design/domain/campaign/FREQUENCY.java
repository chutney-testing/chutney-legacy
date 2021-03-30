package com.chutneytesting.design.domain.campaign;

public enum FREQUENCY {
    HOURLY {
        public final String toString() {
            return "hourly";
        }
    },
    DAILY {
        public final String toString() {
            return "daily";
        }
    },
    WEEKLY {
        public String toString() {
            return "weekly";
        }
    },
    MONTHLY {
        public String toString() {
            return "monthly";
        }
    },

}
