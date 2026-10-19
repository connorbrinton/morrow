import copy
import unittest
import yaml
from prepare_content import validate, ROOT, UniqueLoader
class ContentTests(unittest.TestCase):
    def setUp(self): self.pack = yaml.safe_load((ROOT/'build/content-assets/content.yaml').read_text())
    def test_four_complete_weeks(self):
        validate(self.pack)
        self.assertEqual(len(self.pack['weeks']),4)
        self.assertEqual(self.pack['weeks'][0]['start'],'2026-10-19')
        self.assertEqual(self.pack['weeks'][-1]['days'][-1]['date'],'2026-11-15')
    def test_missing_scripture_rejected(self):
        self.pack['translations']['KJV'].clear()
        with self.assertRaises(AssertionError): validate(self.pack)
    def test_duplicate_dates_rejected(self):
        self.pack['weeks'].append(copy.deepcopy(self.pack['weeks'][0]))
        with self.assertRaises(AssertionError): validate(self.pack)
    def test_wrong_day_rejected(self):
        self.pack['weeks'][0]['days'][0]['date']='2026-10-20'
        with self.assertRaises(AssertionError): validate(self.pack)
    def test_duplicate_yaml_key_rejected(self):
        with self.assertRaises(ValueError): yaml.load('schema: 1\nschema: 2',Loader=UniqueLoader)
if __name__=='__main__': unittest.main()
