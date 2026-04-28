use std::sync::Arc;

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Symbol {
    pub base_asset: Arc<str>,
    pub quote_asset: Arc<str>,
}

impl Symbol {
    pub fn ticker(&self) -> String {
        format!("{}-{}", self.base_asset, self.quote_asset)
    }
}
