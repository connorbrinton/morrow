"""Validate weekly YAML and generate the bundled content asset."""
import datetime as dt
from pathlib import Path
import yaml
ROOT = Path(__file__).resolve().parents[1]
class UniqueLoader(yaml.SafeLoader): pass
def unique_mapping(loader, node, deep=False):
    result = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key in result: raise ValueError(f'Duplicate YAML key: {key}')
        result[key] = loader.construct_object(value_node, deep=deep)
    return result
UniqueLoader.add_constructor(yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, unique_mapping)
def validate(pack):
    assert pack['schema'] == 1, 'Unsupported schema'
    assert pack['weeks'], 'At least one week is required'
    seen = set()
    for week in pack['weeks']:
        start = dt.date.fromisoformat(week['start'])
        assert start.weekday() == 0, 'Weeks start on Monday'
        assert len(week['days']) == 7
        for i, day in enumerate(week['days']):
            assert dt.date.fromisoformat(day['date']) == start + dt.timedelta(days=i)
            assert day['date'] not in seen, 'Duplicate date'
            seen.add(day['date'])
            for field in ('title','reference','scene','discover','explain','younger','older','activity','remember'):
                assert isinstance(day[field],str) and 0 < len(day[field].strip()) < 12000, f'Invalid {field}'
            assert pack['translations']['KJV'].get(day['reference']), 'KJV text is required'
    return pack

def build():
    pack = {'schema':1, 'weeks':[], 'translations':{}}
    for path in sorted((ROOT/'content').glob('*.yaml')):
        data = yaml.load(path.read_text(), Loader=UniqueLoader)
        assert data['schema'] == 1
        pack['weeks'].extend(data['weeks'])
        for name, texts in data['translations'].items():
            dest = pack['translations'].setdefault(name,{})
            for ref, text in texts.items():
                assert ref not in dest or dest[ref] == text, f'Conflicting {name} text: {ref}'
                dest[ref] = text
    pack['weeks'].sort(key=lambda w:w['start'])
    validate(pack)
    output = yaml.safe_dump(pack,sort_keys=False,allow_unicode=True,width=100)
    assert len(output.encode()) <= 2_000_000
    for path in [ROOT/'build/content-assets/content.yaml']:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(output)
    print(f"Validated {len(pack['weeks'])} weeks / {sum(len(w['days']) for w in pack['weeks'])} daily lessons")
if __name__ == '__main__': build()
