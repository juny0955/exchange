#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct Symbol {
    pub base_asset: String,
    pub quote_asset: String,
}

impl Symbol {
    pub fn ticker(&self) -> String {
        format!("{}-{}", self.base_asset, self.quote_asset)
    }
}
