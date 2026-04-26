use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Symbol {
    pub base_asset: String,
    pub quote_asset: String,
}

impl Symbol {
    pub fn ticker(&self) -> String {
        format!("{}-{}", self.base_asset, self.quote_asset)
    }
}
